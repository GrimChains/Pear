package com.example.pear;

import java.util.ArrayList;

public class Identifiers {
	
	private ArrayList<String> IDs;
	
	public Identifiers() {
		IDs = new ArrayList<String>();
	}
	
	public Identifiers(ArrayList<String> copy) {
		IDs = new ArrayList<String>(copy);
	}
	
	public void addId(String id) {
		IDs.add(id);
	}
	
	public ArrayList<String> getIds() {
		return IDs;
	}
	
	public void clearIds() {
		IDs.clear();
	}

}
