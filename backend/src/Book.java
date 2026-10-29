public class Book {
 private final int id; private final String title,author; private boolean available=true;
 public Book(int id,String title,String author){if(id<=0)throw new IllegalArgumentException("Book ID must be positive.");if(title==null||title.isBlank())throw new IllegalArgumentException("Title is required.");if(author==null||author.isBlank())throw new IllegalArgumentException("Author is required.");this.id=id;this.title=title.trim();this.author=author.trim();}
 public int getId(){return id;} public String getTitle(){return title;} public String getAuthor(){return author;} public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;}
}
