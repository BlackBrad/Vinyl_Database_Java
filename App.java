import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;

public class App {
	static User logged_user; //Logged in user;
	static BufferedInputStream stream = new BufferedInputStream(System.in);
	static BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
	
	public static void main(String[] args){
		System.out.println("Bradleys' Vinyl Database Program     V1.0");
		//menu();
		//System.out.println("Thanks for using my Vinyl Database Program");
		Connection connect = connection();

		if (connect == null){
			System.out.println("Could not estabish connection to the database.");
			return;
		}else{
			
		}
		first_menu(connect);
		close_buffered_input();
		close_connection(connect);
	}
	
	public static void main_menu(Connection con){
		boolean quit = false;
		
		while (!quit){
			String menu = "\nChoose an option.\n1. Add a vinyl\n2. Edit vinyl or band\n3. Add to shelf"
					+ "\n4. Search\n5. Profile\n6. Admin panel\n7. Quit\n?";
			String option = get_string(menu);
			switch(option){
			case "1":
				add_vinyl(con);
				break;
			case "2":
				edit_menu(con);
				break;
			case "3":
				//add to your shelf
				break;
			case "4":
				search_menu(con);
				break;
			case "5":
				//profile
				break;
			case"6":
				//admin panel
				break;
			case "7":
				quit = true;
				break;
			default:
				System.out.println("That is not an option.");
			}
		}
	}
	
	public static void first_menu(Connection con){
		//System.out.print("\nChoose an option:\n1. Log-in\n2. Create an account\n?");
		String option = get_string("Choose an option:\n1. Log-in\n2. Create an account\n?");
		
		if (option.equals("1")){
			log_in(con);
		}else if (option.equals("2")){
			create_new_user(con);
		}
	}
	
	public static void add_vinyl(Connection con){
		String name = get_string("Vinyl name: ");
		String band = get_string("Band name: ");
		
		try{
			String check_exists = "SELECT band_id, band_name FROM band WHERE band_name = ?";
			String check_exists_again = "SELECT band_id, album_name FROM vinyl WHERE album_name = ?";

			PreparedStatement statement = con.prepareStatement(check_exists);
			statement.setString(1, band);
			ResultSet result = statement.executeQuery();
			statement = con.prepareStatement(check_exists_again);
			statement.setString(1, name);
			ResultSet vinyls = statement.executeQuery();
			
			while (vinyls.next()){
				while (result.next()){
					if (result.getInt("band_id") == vinyls.getInt("band_id")){
						System.out.println("That is already in the database!");
						return;
					}
				}
				result.beforeFirst();
			}
			
			String year = get_string("Album year: ");
			String label = get_string("Enter the label: ");
			boolean correct_speed = false;
			String speed = get_string("What is the speed?\n1. 33 1/3RPM\n2. 45RPM\n3. 78RPM\n?");
			
			while(!correct_speed){
				switch (speed){
				case "1":
					speed = "33";
					correct_speed = true;
					break;
				case "2":
					speed = "45";
					correct_speed = true;
					break;
				case "3":
					speed = "78";
					correct_speed = true;
					break;
				default:
					System.out.println("That is not a speed.");
					speed = get_string("Try again: ");
				}
			}
			
			//Check to see if band exits in the database
			statement = con.prepareCall("SELECT * FROM band WHERE band_name = ?");
			statement.setString(1, band);
			result.close();
			result = statement.executeQuery();
			boolean band_exists = false;
			while(result.next()){
				if (result.getString("band_name").equals(band)){
					System.out.println("Band found.");
					band_exists = true;
				}
			}
			
			if (!band_exists){
				System.out.println("Band does not exist in databse.\nCreating a profile. Search for " 
						+ band + " to write a band biography.");
				PreparedStatement add_band = con.prepareStatement("INSERT INTO band VALUES (NULL, ? , NULL)");
				add_band.setString(1, band);
				add_band.executeUpdate();
				add_band.close();
			}
			//Get the band id
			int band_id = 0;
			if (true){
				PreparedStatement get_id = con.prepareStatement("SELECT band_id from band WHERE band_name = ?");
				get_id.setString(1, band);
				result.close();
				result = get_id.executeQuery();
				while (result.next()){
					band_id = result.getInt("band_id");
				}
				get_id.close();
			}
			PreparedStatement add_vinyl = con.prepareStatement("INSERT INTO vinyl VALUES (NULL, '" + band_id + "'"
					+ ", ?, " + Integer.parseInt(year) + ", '" + speed + "', '" + label + "')");
			add_vinyl.setString(1, name);
			add_vinyl.executeUpdate();
			System.out.println(name + "(" + year + ")  by " + band + " has been added to the database.");
			add_vinyl.close();
			statement.close();
			result.close();
			vinyls.close();
			
			logged_user.add_to_added();
			
			
		}catch(SQLException e){
			System.out.println(e.getMessage());
			System.out.println("Did not work");
		}
	}
	
	public static void edit_menu(Connection con){
		String option = get_string("What would you like to edit?\n1. Vinyl\n2. Band\n?");
		switch(option){
		case "1":
			edit_vinyl(con);
			break;
		case "2":
			edit_band(con);
			break;
		default:
			System.out.println("That is not an editing option!");
		}
	}
	
	public static void edit_band(Connection con){
		String heading = "EDIT Band Bio";
		String underline = "+" + repeat_char(heading.length(), '-') + "+";
		System.out.println(heading + "\n" + underline);
		try{
			String band = get_string("Band name: ");
			PreparedStatement statement = con.prepareStatement("SELECT band_bio FROM band WHERE band_name=?");
			statement.setString(1, band);
			ResultSet band_result = statement.executeQuery();
			while (band_result.next()){
				String band_bio = band_result.getString("band_bio");
				if (band_bio == null) System.out.println("No band bio.");
				else System.out.println(band_bio);
			}
		}catch(SQLException e){
			System.out.println("Error: " + e.getMessage());
		}
		String name = get_string("Write a bio y/n\n");
		boolean done = false;
		while (!done){
			if (name == "y" || name == "Y"){
				String bio = get_string("Enter bio: ");
				
				////Write bio out to the database!
				
				done = true;
			}
		}
	}
	
	public static void edit_vinyl(Connection con){
		String heading = " EDIT Vinyl";
		String underline = "+" + repeat_char(heading.length(), '-') + "+";
		System.out.println(heading + "\n" + underline);
		try{
			String name = get_string("Album Name: ");
			String band = get_string("Band Name: ");
			PreparedStatement statement = con.prepareStatement("SELECT band_id FROM band WHERE band_name=?");
			statement.setString(1, band);
			ResultSet band_result = statement.executeQuery();
			while (band_result.next()){
				int band_id = band_result.getInt("band_id");
				statement = con.prepareStatement("SELECT * FROM vinyl WHERE band_id=" + band_id + ", album_name=?");
				statement.setString(1, name);
				ResultSet vinyl_result = statement.executeQuery();
				while(vinyl_result.next()){
					System.out.println("Found the album!");
					return;
				}
			}
			System.out.println("Could not find it");
		}catch(SQLException e){
			System.out.println("Error: " + e.getMessage());
		}catch(Exception exept){
			System.out.println("Error: " + exept.getMessage());
		}
	}
	
	public static void search_menu(Connection con){
		String option = get_string("Search for what?\n1. Vinyl\n2. Band\n3. User\n4. I changed my mind\n?");
		switch(option){
		case "1":
			search_for_vinyl(con);
			break;
		case "2":
			search_for_band(con);
			break;
		case "3":
			search_for_user(con);
			break;
		default:
			System.out.println("That is not a searching option.");
		}
	}
	
	public static void search_for_user(Connection con){
		String user = get_string("Enter username: ");
		//Get the searched users information from the database
		try{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM users WHERE username = '" + user + "'");
			ResultSet set = statement.executeQuery();
			
			while(set.next()){
				System.out.println("\nUser Profile");
				System.out.println("+" + repeat_char(set.getString("username").length() + 2, '-') + "+");
				System.out.println("| " + set.getString("username") + " |\n+" + 
				repeat_char(set.getString("username").length() + 2, '-') + "+\n Info:\n");
				if (set.getString("permissions").equals("admin")){
					System.out.println("This user is an admin.");
				}else if (set.getString("permissions").equals("banned")){
					System.out.println("This user is banned.");
				}
				System.out.println("Vinyls added: " + set.getInt("added_vinyls"));
				System.out.println("Vinyls edited: " + set.getInt("edited_vinyls"));
				System.out.println("Users shelf consits of " + set.getInt("shelf") + " vinyls.");
			}
			
		}catch(SQLException e){
			System.out.println("Error: " + e.getMessage());
		}catch(Exception exept){
			System.out.println("Error: " + exept.getMessage());
		}
	}
	
	public static void search_for_vinyl(Connection con){
		String name = get_string("Album name: ");
		try{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM vinyl WHERE album_name = '" + name + "'");
			ResultSet vinyl = statement.executeQuery();
			while (vinyl.next()){
				statement = con.prepareStatement("SELECT band_name FROM band WHERE band_id = " + vinyl.getInt("band_id"));
				ResultSet band = statement.executeQuery();
				while (band.next()){
					String output = "\n" + vinyl.getString("album_name") + "		" + band.getString("band_name") + "		" 
							 + vinyl.getString("year") + "		" + vinyl.getString("speed") + "		" + 
							vinyl.getString("label") + "\n";
					System.out.println(output);
					String nothing = get_string("Hit RETURN to finish: ");
					statement.close();
					vinyl.close();
					band.close();
					return;
				}
			}
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		System.out.println("Could not find " + name + " in the databse.");
	}
	
	public static void search_for_band(Connection con){
		String band = get_string("Band name: ");
		//Get the band data from the database
		try{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM band WHERE band_name = '" + band + "'");
			ResultSet result = statement.executeQuery();
			while (result.next()){
				System.out.println("\nBand Profile");
				System.out.println("+" + repeat_char(result.getString("band_name").length() + 2, '-') + "+");
				System.out.println("| " + result.getString("band_name") + " |\n+" + 
						repeat_char(result.getString("band_name").length() + 2, '-') + "+\nBand Bio:\n");
				if (result.getString("band_bio") == null){
					System.out.println("There is no bio for this band.\n");
				}else{
					System.out.println(result.getString("band_bio"));
				}
				System.out.println("Band releases: \n");
				int id = result.getInt("band_id");
				statement = con.prepareStatement("SELECT * FROM vinyl WHERE band_id = " + id + " ORDER BY year ASC");
				ResultSet vinyls = statement.executeQuery();
				while (vinyls.next()){
					String output = vinyls.getString("album_name") + "        " + result.getString("band_name") + "        " 
							 + vinyls.getString("year") + "        " + vinyls.getString("speed") + "        " + 
							vinyls.getString("label") + "\n";
					System.out.print(output);
				}
			}
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static String repeat_char(int x, char n){
		String data = null;
		String the_char = Character.toString(n);
		for (int i = 0; i < x; i++){
			if (data == null){
				data = the_char;
				i++;
			}
			data += the_char;
		}
		return data;
	}
	
	public static void log_in(Connection con){	
		String message = "Username: ";
		String message_2 = "Password: ";
		String user = get_string(message);
		String pass = get_string(message_2);
		
		try{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM users");
			ResultSet result = statement.executeQuery();
			
			while(result.next()){
				if (result.getString("username").equals(user) && result.getString("password").equals(pass)){
					System.out.println("Correct username and password.\nFetching profile data");
					int id = result.getInt("user_id");
					String email = result.getString("email");
					int added = result.getInt("added_vinyls");
					int edited = result.getInt("edited_vinyls");
					int shelf = result.getInt("shelf");
					String perm = result.getString("permissions");
					logged_user = new User(id, user, pass, email, added, edited, shelf, perm, con);
					
					statement.close();
					result.close();
					main_menu(con);
					return;
				}
			}
			System.out.println("That username & password combination does not exist.");
			
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void create_new_user(Connection con){
		int added, edited, shelf; added = edited = shelf = 0;
		
		String perm = "basic";
		
		String user_name_message = "Desired username: ";
		String pass_message = "Enter a password: ";
		String pass_try_again = "You must enter a password.\nTry again: ";
		String email_message = "Enter your e-mail: ";
		String email_try_again = "You must enter your email.\nTry again: ";
		String user = get_string(user_name_message);
		
		/////////
		//Checking if the entered username already exists or not
		try{
		PreparedStatement statement = con.prepareStatement("SELECT username FROM users");
		ResultSet result = statement.executeQuery();
		
		while(result.next()){
			if (result.getString("username").equals(user)){
				System.out.println("That username already exists!");
				result.close();
				return;
			}else{
				result.close();
				String pass = get_string(pass_message);
				while (pass.length() == 0){
					pass = get_string(pass_try_again);
				}
				
				String email = get_string(email_message);
				while (email.length() == 0){
					email = get_string(email_try_again);
				}
				String query = "INSERT INTO users VALUES (NULL, '" + user + "', '" + pass + "', '" + email + "', " + added + 
						", " + edited + ", " + shelf +  ", '" + perm + "')";
				//System.out.println(query);
				statement = con.prepareStatement(query);
				statement.executeUpdate();
				
				System.out.println("New account has been created");
				statement = con.prepareStatement("SELECT user_id FROM users WHERE username = '" + user + "'");
				result = statement.executeQuery();
				int id = result.getInt("user_id");
				logged_user = new User(id, user, pass, email, added, edited, shelf, perm, con);
				main_menu(con);
				
				statement.close();
				return;
			}
		}
		
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static int get_int(){
		Scanner scan = new Scanner(System.in);
		int data = scan.nextInt();
		scan.close();
		return data;
	}
	
	public static String get_string(String message){
		System.out.print(message);
		String data = null;
		try {
			data = r.readLine();
			//stream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return data;
	}
	
	public static Connection connection(){
		Connection con = null;
		String ip = get_string("What is the databases IP address? ");
		try{
			String host = "jdbc:mysql://" + ip + ":3306/VinylDb?autoReconnect=true&useSSL=false";
			//String host = "jdbc:mysql://localhost:8888/VinylDb?autoReconnect=true&useSSL=false";
			String name = "monty";
			String pass = "python";
	
			con = DriverManager.getConnection(host, name, pass);
			System.out.println("Connection to database established\n");
			return con;
		}catch(SQLException except){
			System.out.println(except.getMessage());
			return null;
		}catch(Exception e){
			System.out.println(e);
			return null;
		}finally{
			
		}
	}
	
	public static void close_buffered_input(){
		try{
			stream.close();
			r.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void close_connection(Connection con){
		try{
			con.close();
			System.out.println("\nConnection to database closed");
		}catch(SQLException e){
			System.out.println(e);
		}catch(Exception except){
			System.out.println(except);
		}
	}
	
}
