package com.eggs.alexa.twitter;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import twitter4j.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailTo:jon.d.elliott@gmail.com">Jon Elliott</a>
 */
@Slf4j
@SuppressWarnings({"unused"})
public class TwitterSearchSpeechlet implements Speechlet{

    private static final String SCREENNAME_SLOT = "screenname";

    private static final String HASHTAG_SLOT = "hashtag";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException { 
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }
    
    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException { 
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse(); 
    }
    
    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        Intent intent = request.getIntent(); 
        String intentName = (intent != null) ? intent.getName() : null;

        if ( "GetUserTimeline".equals(intentName) ) {
            return SpeechletResponse.newTellResponse(
                    processStatuses( searchByScreenName( getTwitter(), intent.getSlot("screenName"),
                            request.getRequestId(), session.getSessionId()),
                    request.getRequestId(), session.getSessionId()));
        }else if ( "SearchByHashtag".equals(intentName) ) {
            return SpeechletResponse.newTellResponse(
                    processStatuses( searchByHashtag( getTwitter(), intent.getSlot("hashtag"),
                            request.getRequestId(), session.getSessionId()),
                            request.getRequestId(), session.getSessionId()));
        }else if( "AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        }
        throw new SpeechletException("Invalid Intent");
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)throws SpeechletException{
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    private SpeechletResponse getHelpResponse() {
        String speechText = "You Retrieve the latest tweets for a user";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }


    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Alexa Twitter Skill";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Twitter");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private String removeUrl(String commentstr){
        String urlPattern =
                "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }

    private Twitter getTwitter() throws SpeechletException {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.verifyCredentials();
            return twitter;
        }catch(TwitterException twe ) {
            throw new SpeechletException(twe);
        }
    }

    private List<Status> searchByScreenName(final Twitter twitter, final Slot slot,
                                            final String requestId, final String sessionId) throws SpeechletException{
        log.info("searchByScreenName requestId={}, sessionId={}, screenName={}", requestId, sessionId, slot.getValue());
        String value = slot.getValue();
        if (StringUtils.isEmpty(value)) {
            throw new SpeechletException("Empty screen name");
        }
        String screenName = "@".concat(value.replace("\\s",""));
        try {
            return twitter.getUserTimeline(screenName);
        }catch(TwitterException twe) {
            log.error("Twitter Exception={}", twe.getMessage());
            throw new SpeechletException(twe);
        }
    }

    private List<Status> searchByHashtag(final Twitter twitter, final Slot slot,
                                         final String requestId, final String sessionId) throws SpeechletException {
        log.info("searchByHashtag requestId={}, sessionId={}, screenName={}", requestId, sessionId, slot.getValue());
        String value = slot.getValue();
        if (StringUtils.isEmpty(value)) {
            throw new SpeechletException("Empty screen name");
        }
        String hashtag = "#".concat(value.replace("\\s",""));
        try {
            QueryResult results = twitter.search(new Query(hashtag));
            return results.getTweets();

        }catch(TwitterException twe) {
            log.error("Twitter Exception={}", twe.getMessage());
            throw new SpeechletException(twe);
        }


    }

    private PlainTextOutputSpeech processStatuses(final List<Status> statuses,
                                                  final String requestId, final String sessionId)
            throws SpeechletException {
        log.info("processStatuses requestId={}, sessionId={}", requestId, sessionId);
        final StringBuilder builder = new StringBuilder();
        statuses.forEach(s -> builder.append(removeUrl(s.getText())).append("\t"));
        PlainTextOutputSpeech output = new PlainTextOutputSpeech();
        output.setText(builder.toString());
        return output;
    }

}
