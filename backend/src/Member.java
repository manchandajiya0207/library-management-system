public abstract class Member {
 private final int id;private final String name,email;
 public Member(int id,String name,String email){if(id<=0)throw new IllegalArgumentException("Member ID must be positive.");if(name==null||name.isBlank())throw new IllegalArgumentException("Name is required.");if(email==null||email.isBlank())throw new IllegalArgumentException("Email is required.");this.id=id;this.name=name.trim();this.email=email.trim();}
 public int getId(){return id;}public String getName(){return name;}public String getEmail(){return email;}
 public abstract int getBorrowingLimit();public abstract int getLoanPeriodDays();public abstract String getType();
}
