package com.techelevator.tenmo;

import com.google.common.reflect.TypeToken;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;
import io.cucumber.java.sl.In;
import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


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

	private void viewCurrentBalance() {
		String url = API_BASE_URL + "balance";

		ResponseEntity<String> balance = restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), String.class);
		console.displayBalance(balance.getBody());
		console.pressEnterToContinue();
	}

	private void viewTransferHistory() {	// -- code added here
     	//show list of transfers from transfer table
		String url = API_BASE_URL + "transfers";
		List<TransferHistory> transfers = null;

		try {
			ResponseEntity<List<TransferHistory>> response =
					restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), new ParameterizedTypeReference<List<TransferHistory>>(){});
			transfers = response.getBody();
		} catch (RestClientResponseException ex) {
				// handles exceptions thrown by rest template and contains status codes
				console.error(ex.getMessage());
			} catch (ResourceAccessException ex) {
				// i/o error, ex: the server isn't running
				console.error(ex.getMessage());
			}

		console.displayTransferHistory(transfers);

		Integer transferID = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");

		if(transferID == 0) {
			return;
		}

		viewTransferDetails(transferID);
	}

	private void viewTransferDetails(int transferId) {
		String url = API_BASE_URL + "transfers/" + transferId;
		TransferDetails transfer = null;
		try {
			ResponseEntity<TransferDetails> response = restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), TransferDetails.class);
			if (response != null) console.displayTransferDetails(response.getBody());
		} catch (RestClientException e) {
			console.error(e.getMessage());
		}
	}

	private	void viewUserList() {	// -- code added here
    	//show list of users from user table
		String url = API_BASE_URL + "users";
		List<User> users = null;

		try {
			ResponseEntity<List<User>> response =
					restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), new ParameterizedTypeReference<List<User>>(){});
			users = response.getBody();
		} catch (RestClientResponseException ex) {
			// handles exceptions thrown by rest template and contains status codes
			console.error(ex.getMessage());
		} catch (ResourceAccessException ex) {
			// i/o error, ex: the server isn't running
			console.error(ex.getMessage());
		}

		console.displayUsers(users);
	}

	private void viewPendingRequests() {	// -- code added here
		//show list of transfers from transfer table with pending status
		String url = API_BASE_URL + "pending";
		List<TransferHistory> pendingRequests = null;

		try {
			ResponseEntity<List<TransferHistory>> response =
					restTemplate.exchange(url, HttpMethod.GET, makeAuthEntity(), new ParameterizedTypeReference<List<TransferHistory>>(){});
					pendingRequests = response.getBody();
		} catch (RestClientResponseException ex) {
			// handles exceptions thrown by rest template and contains status codes
			console.error(ex.getMessage());
		} catch (ResourceAccessException ex) {
			// i/o error, ex: the server isn't running
			console.error(ex.getMessage());
		}

		if (pendingRequests != null) {
			TransferHistory transfer = console.pendingRequestsprompt(pendingRequests);
			if (transfer == null) return;
			int choice = console.approveOrReject(transfer);
			if (choice == 0) return;
			requestResponse(transfer.getTransferId(), choice == 1);
		}
	}

	private void requestResponse(int transferId, boolean isApproved) {
    	String url = API_BASE_URL + "requests/" + transferId;
    	BigOlBoolean approval = new BigOlBoolean();
    	approval.setApproved(isApproved);
    	try {
			ResponseEntity<Integer> response =
					restTemplate.exchange(url, HttpMethod.PUT, makeAuthEntity(approval), Integer.class);
			if (console.updateSuccess(transferId, isApproved)) viewTransferDetails(transferId);
		} catch (RestClientResponseException | ResourceAccessException e) {
    		console.error(e.getMessage());
		}
	}

	private void sendBucks() {	// -code added here

		TransferPayment transfer = new TransferPayment();

		viewUserList();

		Integer personToId = console.getUserInputInteger("Enter ID of user you are sending money to (0 to cancel)");

		if(personToId == 0) {
			return;
		}

		Double amountToTransfer = console.getUserInputDouble("Enter amount");

		transfer.setFromUserId(currentUser.getUser().getId());
		transfer.setToUserId(personToId);
		transfer.setAmount(amountToTransfer);

		ResponseEntity<Integer> response;
		try {
			response = restTemplate.exchange(API_BASE_URL + "transfers", HttpMethod.POST , makeTransferPaymentEntity(transfer), Integer.class);
			Integer transferId = response.getBody();
			if (transferId != null && console.transferSuccess(transferId, false)) viewTransferDetails(transferId);
		} catch (RestClientResponseException | ResourceAccessException e) {
			console.error(e.getMessage());
		}
	}

	private void requestBucks() {

		TransferPayment transfer = new TransferPayment();

		viewUserList();

		Integer personFromID = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");

		if(personFromID == 0) {
			return;
		}

		Double amountToTransfer = console.getUserInputDouble("Enter amount");

		transfer.setToUserId(currentUser.getUser().getId());
		transfer.setFromUserId(personFromID);
		transfer.setAmount(amountToTransfer);

		ResponseEntity<Integer> response;
		try {
			response = restTemplate.exchange(API_BASE_URL + "requests", HttpMethod.POST , makeTransferPaymentEntity(transfer), Integer.class);
			Integer transferId = response.getBody();

			if (transferId != null && console.transferSuccess(transferId, true)) viewTransferDetails(transferId);
		} catch (RestClientResponseException | ResourceAccessException e) {
			console.error(e.getMessage());
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
	 * can be used with get and delete requests
	 * makes http headers and bearer auth
	 * @return http headers
	 */
	private HttpEntity<Void> makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(headers);
	}

	private HttpEntity<BigOlBoolean> makeAuthEntity(BigOlBoolean bool) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(bool, headers);
	}

	/**
	 * post / put entity for Transfer Payment
	 */
	private HttpEntity<TransferPayment> makeTransferPaymentEntity(TransferPayment transfer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(transfer, headers);
	}
}
