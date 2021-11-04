package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private RestTemplate restTemplate = new RestTemplate();


    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {		// -- Denny code added
		String url = API_BASE_URL + "/balance";
//		HttpHeaders headers = new HttpHeaders();
//		headers.setBearerAuth(currentUser.getToken());		Turned these into an auth entity class
//		HttpEntity entity = new HttpEntity<>(headers);
		ResponseEntity<String> balance = restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), String.class);
		console.displayBalance(balance.getBody());
	}

	private Transfer[] viewTransferHistory() {	// -- Denny code added
    	//show list of transfers from transfer table

		Transfer[] transfers = null;
		String url = API_BASE_URL + "/transfers"; //not sure about path name

		try{
			ResponseEntity<Transfer[]> transferHistory =
					restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), Transfer[].class); //should be a list of transfers from the transfers table
			transfers = transferHistory.getBody();
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}

		return transfers;
	}

	private Transfer[] viewPendingRequests() {	// -- Denny code added
		//show list of transfers from transfer table with pending status

		Transfer[] transfers = null;
		String url = API_BASE_URL + "/transfers/pending";

		try{
			ResponseEntity<Transfer[]> transferHistory =
					restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), Transfer[].class); //should be a list of pending transfers from the transfers table
			transfers = transferHistory.getBody();
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}

		return transfers;

	}

	private void sendBucks(Transfer amountTransferred) {	// --Denny code added
    	HttpEntity<Transfer> entity = makeTransferEntity((amountTransferred));
		// should subtract from my account
		try {
			restTemplate.put(API_BASE_URL + amountTransferred.getAccountFrom(), entity);
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}
		//should add to someone elses account
		try {
			restTemplate.put(API_BASE_URL + amountTransferred.getAccountTo(), entity);
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}
	}

	private void requestBucks(Transfer amountTransferred) {		// --Denny Code added
		HttpEntity<Transfer> entity = makeTransferEntity((amountTransferred));
		// should add to my account
		try {
			restTemplate.put(API_BASE_URL + amountTransferred.getAccountTo(), entity);
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}
		//should add to someone elses account
		try {
			restTemplate.put(API_BASE_URL + amountTransferred.getAccountFrom(), entity);
		} catch (RestClientResponseException | ResourceAccessException e) {
			//some kind of output
		}
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}

	/**
	 * Denny Code added
	 * can be used with get and delete requests
	 * makes http headers and bearer auth
	 * @return http headers
	 */
	private HttpEntity<Void> makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(headers);
	}

	/**
	 * Denny code added
	 * post / put entity for auth as well
	 */
	private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(transfer, headers);
	}
}
