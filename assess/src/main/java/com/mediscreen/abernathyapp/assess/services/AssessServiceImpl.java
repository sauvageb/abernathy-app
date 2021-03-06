package com.mediscreen.abernathyapp.assess.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscreen.abernathyapp.assess.dtos.DiabeteAssessmentDTO;
import com.mediscreen.abernathyapp.assess.dtos.PatHistoryTermsCountDTO;
import com.mediscreen.abernathyapp.assess.dtos.PatientAssessmentDTO;
import com.mediscreen.abernathyapp.assess.dtos.PatientHealthInfosDTO;
import com.mediscreen.abernathyapp.assess.models.DiabeteStatus;
import com.mediscreen.abernathyapp.assess.models.DiabeteTerminology;
import com.mediscreen.abernathyapp.assess.proxies.PatHistoryProxy;
import com.mediscreen.abernathyapp.assess.proxies.PatientProxy;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AssessServiceImpl implements AssessService {

    private final PatientProxy patientProxy;
    private final PatHistoryProxy patHistoryProxy;
    private final ObjectMapper mapper;
    private final Logger logger;
    private final AgeCalculatorService ageCalculator;

    @Autowired
    public AssessServiceImpl(PatientProxy patientProxy,
                             PatHistoryProxy patHistoryProxy,
                             AgeCalculatorService ageCalculator,
                             ObjectMapper mapper,
                             Logger logger) {
        this.patientProxy = patientProxy;
        this.patHistoryProxy = patHistoryProxy;
        this.ageCalculator = ageCalculator;
        this.mapper = mapper;
        this.logger = logger;
    }

    @Override
    public DiabeteAssessmentDTO assessPatientDiabeteStatus(String patientId) {
        try {
            EntityModel<PatientHealthInfosDTO> entityModel = patientProxy.getPatient(patientId);
            assert entityModel != null;
            PatientHealthInfosDTO patientHealthInfosDTO = entityModel.getContent();
            assert patientHealthInfosDTO != null;
            return assessPatientDiabeteStatus(patientHealthInfosDTO);
        } catch (AssertionError e) {
            throw new NoSuchElementException();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new InternalError();
        }
    }

    @Override
    public DiabeteAssessmentDTO assessPatientDiabeteStatus(String family, String given) {
        try {
            EntityModel<PatientHealthInfosDTO> entityModel = patientProxy.getPatient(family, given);
            assert entityModel != null;
            PatientHealthInfosDTO patientHealthInfosDTO = entityModel.getContent();
            assert patientHealthInfosDTO != null;
            return assessPatientDiabeteStatus(patientHealthInfosDTO);
        } catch (AssertionError e) {
            throw new NoSuchElementException();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new InternalError();
        }
    }

    // -------------------------------------------------------------- Convenience private methods
    // ------------------------------------------------------------------------------------

    private DiabeteAssessmentDTO assessPatientDiabeteStatus(PatientHealthInfosDTO patientInfos) throws JsonProcessingException {

        ResponseEntity<?> responseEntity = patHistoryProxy.getAssessment(patientInfos.getId(),
                Arrays.stream(DiabeteTerminology.values())
                        .map(DiabeteTerminology::fr)
                        .collect(Collectors.toSet()));

        // Http Status is OK, patHistory response contains PatHistoryTermsCountDTO
        if (responseEntity.getStatusCode() == HttpStatus.OK) {

            int patientAge = ageCalculator.getAge(patientInfos.getDob(), LocalDate.now());
            PatHistoryTermsCountDTO countDTO = mapper.readValue(responseEntity.getBody().toString(), PatHistoryTermsCountDTO.class);
            assert countDTO != null; // always true if request is Ok
            DiabeteStatus diabeteStatus = determineDiabeteStatus(patientInfos.getSex(), patientAge, countDTO.getTermCount());

            return new DiabeteAssessmentDTO(
                    new PatientAssessmentDTO(patientInfos.getFamily(), patientInfos.getGiven(), patientAge),
                    diabeteStatus);
        } else {
            // Http Status is not Ok, there are no patHistory with given patientId
            throw new NoSuchElementException();
        }
    }

    private DiabeteStatus determineDiabeteStatus(String sex, int age, int terminologyCount) {
        return Arrays.stream(DiabeteStatus.values())
                .sorted(Comparator.comparingInt(DiabeteStatus::getEvaluationOrder))
                .filter(d -> d.hasDiabeteStatus(sex, age, terminologyCount))
                .findFirst()
                .orElseThrow();
    }

}
