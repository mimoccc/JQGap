package eu.mjdev.phonegap;

/**
 * The Class AuthenticationToken defines the userName and password to be used for authenticating a web resource
 */
public class AuthenticationToken {
    private String userName;
    private String password;
    
    public String getUserName() { return userName; }
    
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPassword() { return password; }
    
    public void setPassword(String password) { this.password = password; }
}