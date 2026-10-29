const API = '/api';
let books=[], members=[], transactions=[];
const $=id=>document.getElementById(id);
function navigate(page){
 document.querySelectorAll('.page').forEach(p=>p.classList.toggle('active',p.id===page));
 document.querySelectorAll('.nav-item').forEach(b=>b.classList.toggle('active',b.dataset.page===page));
 const titles={dashboard:['Dashboard overview','OVERVIEW'],books:['Book collection','BOOKS'],members:['Library members','MEMBERS'],circulation:['Issue & return desk','CIRCULATION'],transactions:['Transaction history','TRANSACTIONS']};
 $('pageTitle').textContent=titles[page][0];$('crumb').textContent=titles[page][1];
 if(page==='books')renderBooks();if(page==='members')renderMembers();if(page==='transactions')renderTransactions();
}
document.querySelectorAll('.nav-item').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.page)));
function esc(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}
async function api(path, data){
 const options=data?{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams(data)}:{};
 const r=await fetch(API+path,options);const result=await r.json();if(!r.ok||result.error)throw new Error(result.error||'Request failed');return result;
}
function toast(message){const t=$('toast');t.textContent=message;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),3000);}
function showError(err){toast('⚠ '+err.message);}
function closeModal(id){$(id).classList.remove('open')}
function openBookModal(){$('bookModal').classList.add('open')}
function openMemberModal(){$('memberModal').classList.add('open')}
async function loadAll(){
 try{const d=await api('/data');books=d.books;members=d.members;transactions=d.transactions;renderAll();}
 catch(e){showError(e);setTimeout(loadAll,1200);}
}
function renderAll(){renderBooks();renderMembers();renderTransactions();$('statBooks').textContent=books.length;$('statAvailable').textContent=books.filter(b=>b.available).length;$('statMembers').textContent=members.length;$('statLoans').textContent=transactions.filter(t=>t.status==='Active').length;$('navBookCount').textContent=books.length;
 const recent=[...books].slice(-5).reverse();$('recentBooks').innerHTML=recent.length?recent.map(b=>`<tr><td><div class="book-title">${esc(b.title)}</div><div class="book-id">ID #${b.id}</div></td><td>${esc(b.author)}</td><td><span class="status ${b.available?'available':'issued'}">${b.available?'Available':'Issued'}</span></td></tr>`).join(''):'<tr><td colspan="3" class="empty">No books yet</td></tr>';
}
function renderBooks(){const q=($('bookSearch')?.value||'').toLowerCase();const list=books.filter(b=>String(b.id).includes(q)||b.title.toLowerCase().includes(q)||b.author.toLowerCase().includes(q));if(!$('booksTable'))return;
 $('booksTable').innerHTML=list.length?list.map(b=>`<tr><td>#${b.id}</td><td><span class="book-title">${esc(b.title)}</span></td><td>${esc(b.author)}</td><td><span class="status ${b.available?'available':'issued'}">${b.available?'Available':'Issued'}</span></td></tr>`).join(''):'<tr><td colspan="4" class="empty">No matching books found</td></tr>';$('bookResultCount').textContent=`${list.length} books`;
}
function renderMembers(){const q=($('memberSearch')?.value||'').toLowerCase();const list=members.filter(m=>String(m.id).includes(q)||m.name.toLowerCase().includes(q)||m.email.toLowerCase().includes(q));if(!$('membersTable'))return;
 $('membersTable').innerHTML=list.length?list.map(m=>`<tr><td><div class="book-title">${esc(m.name)}</div><div class="book-id">ID #${m.id}</div></td><td>${esc(m.email)}</td><td><span class="status ${m.type==='Student'?'available':'active'}">${esc(m.type)}</span></td><td>${m.limit} books · ${m.loanDays} days</td></tr>`).join(''):'<tr><td colspan="4" class="empty">No members found</td></tr>';$('memberResultCount').textContent=`${list.length} members`;
}
function renderTransactions(){if(!$('transactionsTable'))return;$('transactionsTable').innerHTML=transactions.length?[...transactions].reverse().map(t=>`<tr><td>#${t.id}</td><td>${esc(t.bookTitle)} <span class="book-id">#${t.bookId}</span></td><td>${esc(t.memberName)} <span class="book-id">#${t.memberId}</span></td><td>${esc(t.issueDate)}</td><td>${esc(t.dueDate)}</td><td>${esc(t.returnDate||'—')}</td><td>₹${Number(t.fine).toFixed(2)}</td><td><span class="status ${t.status==='Active'?'active':'closed'}">${t.status}</span></td></tr>`).join(''):'<tr><td colspan="8" class="empty">No transactions yet. Issue a book to get started.</td></tr>';$('txCount').textContent=`${transactions.length} records`;}
$('addBookForm').addEventListener('submit',async e=>{e.preventDefault();const d=Object.fromEntries(new FormData(e.target));try{await api('/books',d);e.target.reset();closeModal('bookModal');await loadAll();toast('Book added successfully.');}catch(err){showError(err)}});
$('addMemberForm').addEventListener('submit',async e=>{e.preventDefault();const d=Object.fromEntries(new FormData(e.target));try{await api('/members',d);e.target.reset();closeModal('memberModal');await loadAll();toast('Member registered successfully.');}catch(err){showError(err)}});
$('issueForm').addEventListener('submit',async e=>{e.preventDefault();const d=Object.fromEntries(new FormData(e.target));try{const r=await api('/issue',d);await loadAll();e.target.reset();toast(`Book issued. Due date: ${r.dueDate}`)}catch(err){showError(err)}});
$('returnForm').addEventListener('submit',async e=>{e.preventDefault();const d=Object.fromEntries(new FormData(e.target));try{const r=await api('/return',d);await loadAll();e.target.reset();toast(`Book returned. Fine: ₹${Number(r.fine).toFixed(2)}`)}catch(err){showError(err)}});
document.querySelectorAll('.modal-backdrop').forEach(m=>m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('open')}));
async function start(){await loadAll();}start();
