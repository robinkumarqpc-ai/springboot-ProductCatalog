package com.productservicing.productservice.Controllers;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sample")
public class SampleController {
    @GetMapping("/{sample_name}")
    public String sample(@PathVariable("sample_name")  String name,@RequestParam("N") int n)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < n; i++) {
            result.append(name);
        }

        return result.toString();
    }



    //<Domain-Name>/sample/1 or <Domain-Name>/sample/2 Domain Name in localhost is localhost:port number
}
