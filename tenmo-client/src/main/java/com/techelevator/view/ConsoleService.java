package com.techelevator.view;


import com.techelevator.tenmo.MoneyMath;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDetails;
import com.techelevator.tenmo.model.TransferHistory;
import com.techelevator.tenmo.model.User;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleService {

	private PrintWriter out;
	private Scanner in;

	public ConsoleService(InputStream input, OutputStream output) {
		this.out = new PrintWriter(output, true);
		this.in = new Scanner(input);
	}

	public Object getChoiceFromOptions(Object[] options) {
		Object choice = null;
		while (choice == null) {
			displayMenuOptions(options);
			choice = getChoiceFromUserInput(options);
		}
		out.println();
		return choice;
	}

	private Object getChoiceFromUserInput(Object[] options) {
		Object choice = null;
		String userInput = in.nextLine();
		try {
			int selectedOption = Integer.valueOf(userInput);
			if (selectedOption > 0 && selectedOption <= options.length) {
				choice = options[selectedOption - 1];
			}
		} catch (NumberFormatException e) {
			// eat the exception, an error message will be displayed below since choice will be null
		}
		if (choice == null) {
			out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
		}
		return choice;
	}

	public void pressEnterToContinue() {
		System.out.println();
		System.out.print("Press enter to continue :");
		in.nextLine();
	}

	private void displayMenuOptions(Object[] options) {
		out.println();
		for (int i = 0; i < options.length; i++) {
			int optionNum = i + 1;
			out.println(optionNum + ") " + options[i]);
		}
		out.print(System.lineSeparator() + "Please choose an option >>> ");
		out.flush();
	}

	public String getUserInput(String prompt) {
		out.print(prompt+": ");
		out.flush();
		return in.nextLine();
	}

	public Integer getUserInputInteger(String prompt) {
		Integer result = null;
		do {
			out.print(prompt+": ");
			out.flush();
			String userInput = in.nextLine();
			try {
				result = Integer.parseInt(userInput);
			} catch(NumberFormatException e) {
				out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
			}
		} while(result == null);
		return result;
	}

	public Double getUserInputDouble(String prompt) {
		Double result = null;
		do {
			out.print(prompt+": ");
			out.flush();
			String userInput = in.nextLine();
			try {
				result = Double.parseDouble(userInput);
			} catch(NumberFormatException e) {
				out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
			}
		} while(result == null);
		return result;
	}

	public void displayBalance(String balance) {
		out.println("Your current account balance is: $" + MoneyMath.format(balance));
	}

	public void displayUsers(List<User> users) {

		bar();
		System.out.println("Users");
		System.out.println("ID		Name");
		bar();
		for (User user : users) { // use clamper
			System.out.println(user.toString());
		}
		bar();
	}

    public void displayTransferHistory(List<TransferHistory> transfers) {

		bar();
		System.out.println("Transfers");
		System.out.println(clampToWidth("ID", 10) + clampToWidth("From/To", 22) + "Amount");
		bar();
		for (TransferHistory transfer : transfers) {
			System.out.print(clampToWidth(String.valueOf(transfer.getTransferId()), 10));
			System.out.print(transfer.isFrom() ? clampToWidth("From:", 6) : clampToWidth("To:", 6));
			System.out.print(clampToWidth(transfer.getUsername(), 16));
			double num = transfer.getAmount();
			String converted = String.valueOf(num);
			System.out.println("$" + MoneyMath.format(converted));
		}
		bar();
	}

	public void displayTransferDetails(TransferDetails transferDetails) {

		bar();
		System.out.println("Transfer Details");
		bar();
		int width = 8;
		System.out.println(clampToWidth("ID:", width) + transferDetails.getTransferId());
		System.out.println(clampToWidth("From:", width) + transferDetails.getFrom());
		System.out.println(clampToWidth("To:", width) + transferDetails.getTo());
		System.out.println(clampToWidth("Type:", width) + transferDetails.getType());
		System.out.println(clampToWidth("Status:", width) + transferDetails.getStatus());
		double num = transferDetails.getAmount();
		String converted = String.valueOf(num);
		System.out.println(clampToWidth("Amount:", width) + "$" + MoneyMath.format(converted));
		pressEnterToContinue();
	}

	public TransferHistory pendingRequestsprompt(List<TransferHistory> requests) {

		if (requests.size() == 0) {
			longBar();
			System.out.println("You have no pending transfer requests at this time.");
			longBar();
			pressEnterToContinue();
			return null;
		}

		while (true) {
			bar();
			System.out.println("Pending Requests");
			System.out.println(clampToWidth("ID", 8) + clampToWidth("From", 16) + "Amount");
			bar();
			Map<Integer,TransferHistory> requestMap = new HashMap<>();
			for (TransferHistory request : requests) {
				System.out.print(clampToWidth(String.valueOf(request.getTransferId()), 8));
				System.out.print(clampToWidth(request.getUsername(), 16));
				System.out.println(MoneyMath.format(String.valueOf(request.getAmount())));
				requestMap.put(request.getTransferId(), request);
			}
			bar();

			int choice = getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
			if (choice == 0) return null;
			if (requestMap.containsKey(choice)) return requestMap.get(choice);
			error("Please provide a valid transfer ID");
		}
	}


	public int approveOrReject(TransferHistory transfer) {
		while (true) {
			bar();
			System.out.println(transfer.getUsername() + " is requesting $" + MoneyMath.format(String.valueOf(transfer.getAmount())));
			bar();
			System.out.println("1: Approve");
			System.out.println("2: Reject");
			System.out.println("0: Take no action");
			shortBar();
			int choice = getUserInputInteger("Please choose an option");
			if (choice == 1 || choice == 2 || choice == 0) return choice;
			error("Please choose one of the given options");
		}
	}

	private void longBar() {
		System.out.println("---------------------------------------------------");
	}

    private void bar() {
		System.out.println("------------------------------------");
	}

	private void shortBar() {
		System.out.println("---------");
	}

	private String clampToWidth(String word, int width) {
		int len = word.length();
		if (len >= width) return len > 2 ? word.substring(0,len-2) + ".." : word.substring(0,len);
		return StringUtils.rightPad(word, width, " ");
	}

	public boolean transferSuccess(int transferId, boolean isRequest) {
		bar();
		String type = isRequest ? "request" : "transfer";
		System.out.println(StringUtils.capitalize(type) + " successful! Your transfer ID is " + transferId + ".");
		return viewTransferDetailsPrompt();
	}

	public boolean updateSuccess(int transferId, boolean isApproved) {
		bar();
		System.out.println("Update successful! You've " + (isApproved ? "approved" : "rejected") + " transfer ID: " + transferId + ".");
		return viewTransferDetailsPrompt();
	}

	private boolean viewTransferDetailsPrompt() {
		while (true) {
			String input = getUserInput("Would you like to view the transfer details? (y/n): ").toLowerCase();
			if (input.equals("y")) return true;
			else if (input.equals("n")) return false;
			else {
				error("Please enter y or n");
			}
		}
	}

	public void error(String message) {
		System.out.println("!!! " + message + " !!!");
	}
}
