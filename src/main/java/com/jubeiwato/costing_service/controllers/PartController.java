package com.jubeiwato.costing_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.services.PartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin
@RestController
@RequestMapping("/parts")
public class PartController {

    private PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getPartTypes() {
        return new ResponseEntity<>(partService.getPartTypes(), HttpStatus.OK);
    }

    @GetMapping("/units")
    public ResponseEntity<List<String>> getPartUnits() {
        return new ResponseEntity<>(partService.getPartUnits(), HttpStatus.OK);
    }
    
    @PostMapping()
    public ResponseEntity<String> createPart(@RequestBody PartRequestDto request) {
        this.partService.createPart(request);
        return new ResponseEntity<>("{\"message\": \"Successful\"}", HttpStatus.OK);
    }
    
}
