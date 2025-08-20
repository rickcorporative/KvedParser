package com.demo.bdd;

import com.demo.core.allure.AllureTools;
import com.demo.core.config.PlaywrightConfig;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepsLogger implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinishedHandler);
    }

    private void stepStartedHandler(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            Logger logger = LoggerFactory.getLogger(event.getTestCase().getName());
            PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
            String keyWord = step.getStep().getKeyword();
            String stepText = step.getStep().getText();
            logger.info(this.formatString(keyWord, stepText));
        }
    }

    private void stepFinishedHandler(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            if(event.getResult().getStatus().is(Status.FAILED)){
                AllureTools.attachScreenshot(PlaywrightConfig.getPage());
            }
        }
    }

    private String formatString(String keyWord, String stepText){
        return keyWord.toUpperCase() +
                ": " +
                stepText;
    }
}
