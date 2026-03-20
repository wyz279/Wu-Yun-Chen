package org.groupweb.vscode;

import java.io.IOException;
import java.util.ArrayList;

public class WebPage {

    // attributes
	public String url;
	public String name;
	public WordCounter counter;
	public double score;

	// constructor
	public WebPage(String url,String name){
		this.url = url;
		this.name = name;
		this.counter = new WordCounter(new WebNode(this));	
	}
	
    // methods
	public void setScore(ArrayList<Keyword> keywords) throws IOException{
		// 1. calculate the score of this webPage
		this.score = counter.computeScore(keywords);    
	}
	
}
