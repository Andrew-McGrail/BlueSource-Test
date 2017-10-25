package com.orasi.bluesource;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.FindBy;

import com.orasi.web.OrasiDriver;
import com.orasi.web.webelements.Link;
import com.orasi.web.webelements.impl.internal.ElementFactory;

public class Header {
	private OrasiDriver driver = null;
	
	/**Page Elements**/
	@FindBy(linkText = "Accounts") private Link lnkAccounts;
	@FindBy(xpath = "//li[contains(.,'Employees')]/a") private Link lnkEmployees;
	
	/**Constructor**/
	public Header(OrasiDriver driver){
		this.driver = driver;
		ElementFactory.initElements(driver, this);
	}
	
	/**Page Interactions**/

	/**
	 * This method navigates to Accounts page
	 * @author Paul
	 */
	public void navigateAccounts(){
		
		boolean accountBool = lnkAccounts.isDisplayed();
		
		//Displaying the boolean current status
		System.out.println("Output from account bool: " + accountBool);
		
		//Some users do not have the accounts permissions so the tab isnt present
		try
		{
			if(accountBool == true)
			{
				lnkAccounts.click();
				System.out.println("This user has account permissions");
			}
			else if (accountBool == false)
			{
				System.out.println("The user does not have account permissions");
			}
		}
		catch(NoSuchElementException e)
		{
			System.out.println("Element can not be found for accounts tab \n" + e.getLocalizedMessage());
		}
	}

	/**
	 * This method navigates to Employees page
	 * @author Paul
	 */
	public void navigateEmployees() {
		lnkEmployees.click();		
	}

}