package com.techelevator.view;


import com.techelevator.tenmo.MoneyMath;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDetails;
import com.techelevator.tenmo.model.TransferHistory;
import com.techelevator.tenmo.model.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
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
		out.println("Your current account balance is: $" + balance);
	}

	public void displayUsers(List<User> users) {

		bar();
		System.out.println("Users");
		System.out.println("ID		Name");
		bar();
		for (User user : users) {
			System.out.println(users.toString());
		}
		bar();
	}

    public void displayTransferHistory(List<TransferHistory> transfers) {

		bar();
		System.out.println("Transfers");
		System.out.println("ID      From/To            Amount");
		bar();
		for (TransferHistory transfer : transfers) {
			System.out.println(transfer.toString());
		}
		bar();
	}

	public void displayTransferDetails(TransferDetails transferDetails) {

		bar();
		System.out.println("Transfer Details");
		bar();
		System.out.println(transferDetails.toString());
	}

	public void displayPendingRequests(String pendingRequests) {
		System.out.println(pendingRequests);
	}

    private void bar() {
		System.out.println("------------------------------------");
	}
}
