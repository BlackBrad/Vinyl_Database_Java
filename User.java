import java.sql.*;

public class User {
	private int ID;
	private String user;
	private String pass;
	private String email;
	private int added;
	private int edited;
	private int shelf;
	Connection con;
	
	////////////////////////
	// Chart of Permissions
	// 0 = admin
	// 1 = basic
	// 2 = banned
	//
	private int perm;
	
	//Constructor
	public User(int ID, String user, String pass, String email, int added, int edited, int shelf, String perm, Connection con){
		this.ID = ID;
		this.user = user;
		this.pass = pass;
		this.email = email;
		this.added = added;
		this.edited = edited;
		this.shelf = shelf;
		this.con = con;
		
		
		if (perm.equals("basic")){
			this.perm = 1;
		}else if(perm.equals("admin")){
			this.perm = 0;
		}else if (perm.equals("banned")){
			this.perm = 2;
		}
		
		System.out.println("TESTING: New User created");
	}
	
	//Getters
	public int get_id(){
		return ID;
	}
	
	public int get_perm(){
		return perm;
	}
	
	//User functions that only an admin can access
	public void set_user(String user){
		this.user = user;
		update();
	}
	
	public void set_pass(String pass){
		this.pass = pass;
		update();
	}
	
	public void set_perm(int perm){
		this.perm = perm;
		update();
	}
	
	
	//User Functions that a user can indirectly use
	public void set_email(String email){
		this.email = email;
		update();
	}
	
	public void set_shelf(int shelf){
		this.shelf = shelf;
		update();
	}
	
	public void add_to_added(){
		added++;
		update();
	}
	
	public void add_to_edited(){
		edited++;
		update();
	}
	
	public void add_to_shelf(){
		shelf++;
		update();
	}
	
	//When the user quits update all profile data
	public void update(){
		try{
			String update_user_sql = "UPDATE users SET email='" + email + "', added_vinyls=" + added + ", "
					+ "edited_vinyls=" + edited + ", shelf=" + shelf + " WHERE user_id=" + ID;
			PreparedStatement statement = con.prepareStatement(update_user_sql);
			statement.executeUpdate();
			System.out.println("Profile Updated");
			
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}
}
