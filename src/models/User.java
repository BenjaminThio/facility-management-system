package src.models;

public class User {
    public enum Role
    {
        STUDENT(0), STAFF(1), ADMIN(2);

        int val = 0;

        private Role(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }

        public static Role cast(int val)
        {
            for (Role role : Role.values())
            {
                if (role.getVal() == val)
                {
                    return role;
                }
            }
            return null;
        }

        public static Role cast(String val)
        {
            for (Role role : Role.values())
            {
                if (role.name() == val)
                {
                    return role;
                }
            }
            return null;
        }
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

    public String getName()
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