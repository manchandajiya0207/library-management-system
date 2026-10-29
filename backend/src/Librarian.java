import java.time.LocalDate;import java.util.*;
public class Librarian {
 private final List<Book> books=new ArrayList<>();private final List<Member> members=new ArrayList<>();private final List<Transaction> transactions=new ArrayList<>();private final FineCalculator calculator=new StandardFineCalculator();private int nextId=1;
 public void addBook(Book b)throws LibraryException{if(findBook(b.getId())!=null)throw new LibraryException("Book ID already exists.");books.add(b);}
 public void registerMember(Member m)throws LibraryException{if(findMember(m.getId())!=null)throw new LibraryException("Member ID already exists.");members.add(m);}
 public Book findBook(int id){for(Book b:books)if(b.getId()==id)return b;return null;}public Member findMember(int id){for(Member m:members)if(m.getId()==id)return m;return null;}
 public List<Book> getBooks(){return new ArrayList<>(books);}public List<Member> getMembers(){return new ArrayList<>(members);}public List<Transaction> getTransactions(){return new ArrayList<>(transactions);}
 public void issueBook(int bid,int mid)throws LibraryException{Book b=findBook(bid);Member m=findMember(mid);if(b==null)throw new LibraryException("Book ID not found.");if(m==null)throw new LibraryException("Member ID not found.");if(!b.isAvailable())throw new LibraryException("Book is already issued.");long active=transactions.stream().filter(t->t.isActive()&&t.getMember().getId()==mid).count();if(active>=m.getBorrowingLimit())throw new LibraryException("Member borrowing limit reached.");transactions.add(new Transaction(nextId++,b,m));b.setAvailable(false);}
 public double returnBook(int bid,int mid)throws LibraryException{for(Transaction t:transactions)if(t.isActive()&&t.getBook().getId()==bid&&t.getMember().getId()==mid){t.close(LocalDate.now(),calculator);t.getBook().setAvailable(true);return t.getFine();}throw new LibraryException("No active loan found for this book/member.");}
}
