package com.productservicing.productservice.Service;

import com.productservicing.productservice.DTOS.FakeStoreProductDTO;
import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Service//("FakeStoreProductService")
@Primary
public class FakeStoreProductService implements ProductService{
    //This Service Class Will Get Product info by interacting with 3rd party API-FakseStore.

    private RestTemplate restTemplate;
    private RedisTemplate<String,Object> redisTemplate;

    public FakeStoreProductService(RestTemplate restTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundExceptions {
        Product product;
        //first check if product with given id is there in cache or not , if yes return from cache
        product=(Product) redisTemplate.opsForHash().get("PRODUCTS","PRODUCT_"+productId.toString());
        if(product!=null)
            return product;
        //on cache miss return from below
        //throw new RuntimeException("Something went wrong");

        ResponseEntity<FakeStoreProductDTO> fakeStoreProductDTOResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products/"+productId, FakeStoreProductDTO.class);
        FakeStoreProductDTO fakeStoreProductDTO=fakeStoreProductDTOResponseEntity.getBody();

        //Parse FakeStoreProductDTO to Product
        //throw new RuntimeException("Something went wrong");
        if(fakeStoreProductDTO==null)
            throw  new ProductNotFoundExceptions("Product:" + productId + "Not Found");
        //before returning store in redis first
        product=parseFakeStoreProductDTOToProduct(fakeStoreProductDTO);
        redisTemplate.opsForHash().put("PRODUCTS","PRODUCT_"+productId.toString(),product);
        return product;
        //return parseFakeStoreProductDTOToProduct(fakeStoreProductDTO);


    }
    //Parse FakeStoreProductDTO to Product
    private static Product parseFakeStoreProductDTOToProduct(FakeStoreProductDTO fakeStoreProductDTO){
        if(fakeStoreProductDTO==null)
            return null;
        Product product=new Product();
        product.setId(fakeStoreProductDTO.getId());
        product.setTitle(fakeStoreProductDTO.getTitle());
        product.setPrice(fakeStoreProductDTO.getPrice());
        product.setImageURL(fakeStoreProductDTO.getImage());
        product.setDescription(fakeStoreProductDTO.getDescription());
        Category category=new Category();
        category.setName(fakeStoreProductDTO.getCategory());
        product.setCategory(category);
        return product;
    }
    @Override
    public List<Product> getAllProducts() {
        /*
        ResponseEntity<List<FakeStoreProductDTO>> fakeStoreProductDtoListResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products",
                List<FakeStoreProductDTO>.class  // compilation error as at run time generic type is erased so its class cant be derived
        );

        */
        //instead of List<FakeStoreProductDto>-Generic use array of FakeStoreProductDto which is not generics
        ResponseEntity<FakeStoreProductDTO[]> fakeStoreProductDtoListResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products",
                FakeStoreProductDTO[].class
        );
        FakeStoreProductDTO[] fakeStoreProductDtoList=fakeStoreProductDtoListResponseEntity.getBody();
        List<Product> products=new ArrayList<>();
        for(FakeStoreProductDTO fakeStoreProductDTO:fakeStoreProductDtoList){
            products.add(parseFakeStoreProductDTOToProduct(fakeStoreProductDTO));
        }
        return products;
    }

    @Override
    public Product createProduct(Product product) throws CategoryNotFoundException {
        return null;
    }

    
    @Override
    public void deleteProduct(Long productId) {

    }

    //FakeStoreProductService has no DB to push sorting/pagination down to, so filter/sort/paginate in memory over the full product list.
    @Override
    public Page<Product> getProductsByTitle(String title, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        List<Product> filteredProducts = getAllProducts().stream()
                .filter(product -> product.getTitle() != null && product.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

        Comparator<Product> comparator = switch (sortBy) {
            case "price" -> Comparator.comparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
            case "id" -> Comparator.comparing(Product::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "description" -> Comparator.comparing(Product::getDescription, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Product::getTitle, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        filteredProducts.sort(comparator);

        int fromIndex = Math.min(pageNumber * pageSize, filteredProducts.size());
        int toIndex = Math.min(fromIndex + pageSize, filteredProducts.size());
        List<Product> pageContent = filteredProducts.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, PageRequest.of(pageNumber, pageSize), filteredProducts.size());
    }
}
