import java.time.LocalDate;public class Transaction {
 private final int id;private final Book book;private final Member member;private final LocalDate issueDate,dueDate;private LocalDate returnDate;private double fine;
 public Transaction(int id,Book b,Member m){this.id=id;book=b;member=m;issueDate=LocalDate.now();dueDate=issueDate.plusDays(m.getLoanPeriodDays());}
 public int getId(){return id;}public Book getBook(){return book;}public Member getMember(){return member;}public LocalDate getIssueDate(){return issueDate;}public LocalDate getDueDate(){return dueDate;}public LocalDate getReturnDate(){return returnDate;}public double getFine(){return fine;}public boolean isActive(){return returnDate==null;}
 public void close(LocalDate date,FineCalculator c){if(!isActive())throw new IllegalStateException("Transaction already closed.");if(date.isBefore(issueDate))throw new IllegalArgumentException("Invalid return date.");returnDate=date;fine=c.calculateFine(dueDate,date);}
}
