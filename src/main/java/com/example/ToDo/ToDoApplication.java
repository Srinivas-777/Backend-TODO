package com.example.ToDo;

import com.example.ToDo.Helper.MyAnalyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
public class ToDoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToDoApplication.class, args);
		stemExample(); // Call the method to demonstrate stemming
	}

	private static void stemExample() {
		// Dynamic stop words list (can be replaced with input from a file or database)
		List<String> dynamicStopWords = Arrays.asList("men"); // Add your dynamic stop words here
		CharArraySet stopWords = new CharArraySet(dynamicStopWords, true); // Create CharArraySet from the list

		MyAnalyzer myAnalyzer = new MyAnalyzer(stopWords, new HashSet<>());
		System.out.println(myAnalyzer.stem("nikes shoes for mens"));
	}
}