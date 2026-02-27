package src;
class User {
    enum Role
    {
        STUDENT,
        STAFF,
        ADMIN
    }
    private String name;
    private String email;
    private String password;
    private Role role;

    public User(String name, String email, String password, Role role)
    {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getname()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Role getRole()
    {
        return this.role;
    }
}