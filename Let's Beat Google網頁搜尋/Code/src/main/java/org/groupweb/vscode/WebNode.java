package org.groupweb.vscode;

import java.io.IOException;
import java.util.ArrayList;

public class WebNode {

    // attributes
    public WebNode parent;
    public ArrayList<WebNode> children;
    public WebPage webPage; // child element
    public double nodeScore; // main element This node's score += all its children・s nodeScore

    // constructor
    public WebNode(WebPage webPage){
		this.webPage = webPage;
		this.children = new ArrayList<WebNode>();
	}

    // methods
    public void addChild(WebNode child){
		//add the WebNode to its children list
		this.children.add(child);
		child.parent = this;
	}
	
	public boolean isTheLastChild(){
		if(this.parent == null) return true;
		ArrayList<WebNode> siblings = this.parent.children;
		
		return this.equals(siblings.get(siblings.size() - 1));
	}

    // setter
    public void setNodeScore(ArrayList<Keyword> keywords) throws IOException{
		// 1. compute the score of this webPage
		webPage.setScore(keywords);
    	double webPageScore = webPage.score; // grab computed score
		// 2. initialize nodeScore with self score
		this.nodeScore = webPageScore;
		// 3. accumulate weighted children nodeScore (postorder traversal)
		for(WebNode child : children){
			child.setNodeScore(keywords);
			this.nodeScore += child.nodeScore * 0.6; // apply decay weight 0.6
		}
	}

	// getter
	public int getDepth(){
		int retVal = 1;
		WebNode currNode = this;
		while(currNode.parent!=null){
			retVal ++;
			currNode = currNode.parent;
		}
		return retVal;
	}
}
