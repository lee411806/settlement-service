package com.sparta.settlementservice.user.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/")
    @ResponseBody
    public String mainAPI() {

        System.out.println(entityManager.getProperties().get("hibernate.physical_naming_strategy"));
        return "main route";
    }
}