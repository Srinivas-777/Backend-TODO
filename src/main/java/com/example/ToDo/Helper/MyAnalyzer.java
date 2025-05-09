package com.example.ToDo.Helper;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
public class MyAnalyzer extends Analyzer {

    private final CharArraySet stopWords;
    private final Set<String> protectedTerms;

    public MyAnalyzer(CharArraySet stopWords, Set<String> protectedTerms) {
        this.stopWords = stopWords;
        this.protectedTerms = protectedTerms;
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(); // break into individual tokens
        TokenStream tokenStream = new StopFilter(tokenizer, stopWords); // remove stop words
        tokenStream = new TokenFilter(tokenStream) {// remove protected terms
            @Override
            public boolean incrementToken() throws IOException {// override the incrementToken method
                if(!input.incrementToken()) return false;// if the input stream has no more tokens
                CharTermAttribute charTermAttribute = getAttribute(CharTermAttribute.class);// get the token text
                String term = charTermAttribute.toString();// get the token text
                if(protectedTerms.contains(term)) return false;// skip the protected terms
                return true;// return the token
            }
        };
        tokenStream = new PorterStemFilter(tokenStream); // stem words
        return new TokenStreamComponents(tokenizer, tokenStream); // return the components
    }

    public String stem(String text){
        protectedTerms.add("abcd");
        if(text == null || text.isEmpty()) return null;// return null if the text is null or empty
        Pair<String, String> placeHolders = StemmerHelper.getPlaceHolders(text, protectedTerms);// placeHolders = Pair.of("___ world", "hello world")
        text = placeHolders.getFirst();// text = "hello world"
        try(TokenStream tokenStream = tokenStream(null, text)){ // null is the field
            StringBuilder sb = new StringBuilder(); // collect the stemmed tokens
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class); // get the token text
            tokenStream.reset(); // reset the token stream
            while(tokenStream.incrementToken()){ // iterate over the tokens
                String term = charTermAttribute.toString(); // get the token text
                if(protectedTerms.contains(term)) continue; // skip the protected terms
                sb.append(term).append(" "); // append the stemmed token to the StringBuilder
            }
            tokenStream.end(); // end the token stream
            if(placeHolders.getSecond() != null) text = text.replace("_", placeHolders.getSecond());
            return sb.toString().trim(); // return the stemmed text
        }
        catch(IOException e){ // handle any exceptions
            return new RuntimeException(e).getMessage();// return the exception message
        }
    }
}
