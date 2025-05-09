package com.example.ToDo.Helper;


import org.springframework.data.util.Pair;

import java.util.Set;

public class StemmerHelper {
    public static Pair<String,String> getPlaceHolders(String text, Set<String> set){// text = "hello world"
        String var = null;// var = "hello world"
        for(String s : set){// s = "hello"
            var = text;//  var = "hello world"
            if(text.contains(s)) text = text.replace(s, "_");// text = "___ world"
        }
        return Pair.of(text, var);// return Pair.of("___ world", "hello world")
    }
}
