package com.mediscreen.abernathy.client.controllers;

import com.mediscreen.abernathy.client.dtos.PatHistoryDTO;
import com.mediscreen.abernathy.client.proxies.AppPatHistoryProxy;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

public class PatHistoryController {


    private final Logger logger;
    private final AppPatHistoryProxy appPatHistoryProxy;

    @Autowired
    public PatHistoryController(@Qualifier("getPatHistoryLogger") Logger logger, AppPatHistoryProxy appPatHistoryProxy) {
        this.logger = logger;
        this.appPatHistoryProxy = appPatHistoryProxy;
    }


    @GetMapping(value = "/patHistory/update")
    public String updatePatHistoryForm(@RequestParam("id") String id,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        EntityModel<PatHistoryDTO> patHistory = appPatHistoryProxy.getPatHistory(id);
        if (patHistory == null) {
            redirectAttributes.addFlashAttribute("patHistoryNotFound", true);
            return "redirect:patient/list";
        }
        model.addAttribute("patHistoryToUpdate", patHistory.getContent());
        return "patHistory/update";
    }

    @PostMapping(value = "/patHistory/update")
    public String updatePatHistoryAction(@Valid @ModelAttribute("patientToUpdate") PatHistoryDTO patHistoryDTO,
                                         BindingResult result,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        if (patHistoryDTO.getId() == null) {
            result.addError(new FieldError("patHistoryToUpdate", "id", "ID is mandatory."));
        }

        if (result.hasErrors()) {
            model.addAttribute("patHistoryToUpdate", patHistoryDTO);
            return "patient/update";
        } else {
            appPatHistoryProxy.updatePatHistory(
                    patHistoryDTO.getId(),
                    patHistoryDTO.getPatientId(),
                    patHistoryDTO.getContent()
            );
            redirectAttributes.addFlashAttribute("paHistoryUpdated", true);
        }

        return "redirect:/patient/get?id=" + patHistoryDTO.getPatientId();
    }

    @GetMapping(value = "/patHistory/add")
    public String addPatHistoryForm(@RequestParam("patientId") String patientId,
                                    Model model) {
        model.addAttribute("patHistoryToAdd", new PatHistoryDTO(null, patientId, null));
        return "patHistory/add";
    }

    @PostMapping(value = "/patHistory/add")
    public String addPatHistoryAction(@Valid @ModelAttribute("patientToAdd") PatHistoryDTO patHistoryDTO,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("patHistoryToAdd", patHistoryDTO);
            return "patient/add";
        } else {
            appPatHistoryProxy.addPatHistory(
                    patHistoryDTO.getPatientId(),
                    patHistoryDTO.getContent()
            );
            redirectAttributes.addFlashAttribute("patHistoryAdded", true);
            return "redirect:/patient/get?id=" + patHistoryDTO.getPatientId();
        }
    }

}