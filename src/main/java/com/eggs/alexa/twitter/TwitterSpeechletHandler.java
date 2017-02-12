package com.eggs.alexa.twitter;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailTo:jon.d.elliott@gmail.com">Jon Elliott</a>
 */
@SuppressWarnings({"unused"})
public class TwitterSpeechletHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<>();

    static {
          /*
           * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
           * Alexa Skill and put the relevant Application Ids in this Set.
           */
        supportedApplicationIds.add("amzn1.ask.skill.443aba1d-ede6-433e-ba51-eb16dd6e7344");
    }

    public TwitterSpeechletHandler() {
        super(new TwitterSearchSpeechlet(), supportedApplicationIds);
    }
}
