package leo.webcrawler.main;

import leo.webcrawler.main.controller.ApplicationController;

/**
 * 
 * The purpose of this class is
 * 
 * @author leoky
 *
 */
public class Main {
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		
		ApplicationController applicationController = new ApplicationController();
		applicationController.perform();
	}
}
