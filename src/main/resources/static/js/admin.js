

// --- 1. Constants & Config ---
const ADMIN_TOKEN_KEY = 'adminToken';
const ADMIN_LOGIN_URL = '/admin/login.html';

// Wrapper for common fetchWithAuth
async function fetchWithAuth(url, options = {}) {
    return await apiFetchWithAuth(url, options, ADMIN_TOKEN_KEY, ADMIN_LOGIN_URL);
}

function isPaidOrder(order) {
    return ['PAID', 'SUCCESS', 'COMPLETED'].includes((order.paymentStatus || '').toUpperCase());
}

// --- 2. Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('/admin/') && !window.location.pathname.includes('/login')) {
        if (!localStorage.getItem(ADMIN_TOKEN_KEY)) {
            window.location.href = ADMIN_LOGIN_URL;
        }
    }
});

// --- 3. UI Handlers & Core Logic ---

function handleLogout() {
    let modal = document.getElementById('logoutConfirmModal');
    
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'logoutConfirmModal';
        modal.className = 'modal-overlay';
        modal.style.display = 'none';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100%';
        modal.style.height = '100%';
        modal.style.background = 'rgba(0,0,0,0.5)';
        modal.style.zIndex = '2000';
        modal.style.justifyContent = 'center';
        modal.style.alignItems = 'center';
        
        modal.innerHTML = `
            <div class="card" style="width: 100%; max-width: 400px; padding: 2rem; text-align: center; background: var(--bg-card);">
                <div style="font-size: 3rem; color: var(--danger); margin-bottom: 1rem;">
                    <i class="fa-solid fa-right-from-bracket"></i>
                </div>
                <h3 style="margin-bottom: 0.5rem; font-size: 1.5rem; color: var(--text-main);">Confirm Logout</h3>
                <p style="color: var(--text-muted); margin-bottom: 2rem;">Are you sure you want to logout of the admin portal?</p>
                <div style="display: flex; gap: 10px; justify-content: center;">
                    <button class="btn btn-outline" id="btnCancelLogout" style="width: 100%;">Cancel</button>
                    <button class="btn btn-primary" id="btnConfirmLogout" style="width: 100%; background: var(--danger); border-color: var(--danger);">Logout</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        
        document.getElementById('btnCancelLogout').addEventListener('click', () => {
            modal.style.display = 'none';
        });
        
        document.getElementById('btnConfirmLogout').addEventListener('click', () => {
            localStorage.removeItem('adminToken');
            window.location.href = '/admin/login.html';
        });
    }
    
    modal.style.display = 'flex';
}

// Global Date Filter State
let currentStartDate = null;
let currentEndDate = null;

function updateGlobalDateFilter() {
    const filterEl = document.getElementById('dateFilter');
    const customDateEl = document.getElementById('customDateFilter');
    
    let val = filterEl ? filterEl.value : 'all';
    let customDate = customDateEl ? customDateEl.value : '';
    
    const now = new Date();
    let start = new Date();
    
    // Format local time to ISO string without UTC conversion
    const formatLocalDateTime = (date) => {
        const pad = n => n.toString().padStart(2, '0');
        return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
    };
    
    if (customDate) {
        start = new Date(customDate);
        start.setHours(0,0,0,0);
        const end = new Date(customDate);
        end.setHours(23,59,59,999);
        currentStartDate = formatLocalDateTime(start);
        currentEndDate = formatLocalDateTime(end);
    } else if (val === 'today') {
        start.setHours(0,0,0,0);
        now.setHours(23,59,59,999);
        currentStartDate = formatLocalDateTime(start);
        currentEndDate = formatLocalDateTime(now);
    } else if (val === 'week') {
        start.setDate(now.getDate() - 7);
        start.setHours(0,0,0,0);
        now.setHours(23,59,59,999);
        currentStartDate = formatLocalDateTime(start);
        currentEndDate = formatLocalDateTime(now);
    } else if (val === 'month') {
        start.setMonth(now.getMonth() - 1);
        start.setHours(0,0,0,0);
        now.setHours(23,59,59,999);
        currentStartDate = formatLocalDateTime(start);
        currentEndDate = formatLocalDateTime(now);
    } else {
        currentStartDate = null;
        currentEndDate = null;
    }
    
    // Refresh Current Page
    if(window.location.pathname.includes('/dashboard')) loadDashboard();
    if(window.location.pathname.includes('/orders')) loadOrders();
    if(window.location.pathname.includes('/payments')) loadPayments();
}

// Dashboard Logic
async function loadDashboard() {
    try {
        let url = `${API_BASE}/admin/dashboard/stats`;
        if (currentStartDate) url += `?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const res = await fetchWithAuth(url);
        if (res.ok) {
            const stats = await res.json();
            document.getElementById('statOrders').textContent = stats.totalOrders;
            document.getElementById('statRevenue').textContent = `₹${stats.totalRevenue}`;
            document.getElementById('statPayments').textContent = stats.totalPayments;
            document.getElementById('statPending').textContent = stats.pendingOrders;
        }
        
        // Load Tables
        loadDashboardOrders();
        loadDashboardPayments();
    } catch(e) {
        console.error("Dashboard error", e);
    }
}

async function loadDashboardOrders() {
    try {
        let url = `${API_BASE}/orders`;
        if (currentStartDate) url += `?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const res = await fetchWithAuth(url);
        if(res.ok) {
            let allOrders = await res.json();
            const orders = allOrders;
            const tbody = document.getElementById('recentOrdersBody');
            tbody.innerHTML = '';
            
            const recent = orders.slice(0, 5); // top 5
            if(recent.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No recent orders</td></tr>`;
                return;
            }
            
            recent.forEach(order => {
                const statusMap = {
                    'PENDING': 'status-pending',
                    'PREPARING': 'status-preparing',
                    'READY': 'status-ready',
                    'COMPLETED': 'status-completed',
                    'CANCELLED': 'status-cancelled'
                };
                tbody.innerHTML += `
                <tr>
                    <td style="font-weight:600; color:var(--primary);">#CB${order.id}</td>
                    <td>${order.studentName || order.studentEmail}</td>
                    <td>${order.items.reduce((sum,i)=>sum+i.quantity,0)} items</td>
                    <td style="font-weight:600;">₹${order.totalAmount}</td>
                    <td><span class="status-badge ${statusMap[order.status]||'status-pending'}">${order.status}</span></td>
                </tr>`;
            });
        }
    } catch(e) {}
}

async function loadDashboardPayments() {
    try {
        let url = `${API_BASE}/payments`;
        if (currentStartDate) url += `?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const res = await fetchWithAuth(url);
        if(res.ok) {
            const payments = await res.json();
            const tbody = document.getElementById('recentPaymentsBody');
            tbody.innerHTML = '';
            
            const recent = payments.slice(0, 5); // top 5
            if(recent.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No recent payments</td></tr>`;
                return;
            }
            
            recent.forEach(pay => {
                const date = new Date(pay.createdAt).toLocaleDateString();
                const statusClass = pay.status === 'PAID' ? 'status-completed' : 'status-failed';
                tbody.innerHTML += `
                <tr>
                    <td style="font-weight:600;">#PAY${pay.id}</td>
                    <td style="font-weight:600; color:var(--primary);">#CB${pay.orderId}</td>
                    <td style="font-weight:600;">₹${pay.amount}</td>
                    <td><span class="status-badge ${statusClass}">${pay.status}</span></td>
                    <td>${date}</td>
                </tr>`;
            });
        }
    } catch(e) {}
}

// Menu Management Logic
let allFoodItems = [];

async function loadMenu() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/food`);
        if (res.ok) {
            allFoodItems = await res.json();
            
            // Update Stats
            document.getElementById('statTotalMenu').textContent = allFoodItems.length;
            document.getElementById('statBreakfastItems').textContent = allFoodItems.filter(f=>f.category && f.category.toLowerCase().includes('breakfast')).length;
            document.getElementById('statLunchItems').textContent = allFoodItems.filter(f=>f.category && f.category.toLowerCase().includes('lunch')).length;
            document.getElementById('statSnacksItems').textContent = allFoodItems.filter(f=>f.category && f.category.toLowerCase().includes('snack')).length;
            document.getElementById('statBevItems').textContent = allFoodItems.filter(f=>f.category && f.category.toLowerCase().includes('beverage')).length;
            
            renderMenuTable(allFoodItems);
        }
    } catch(e) {}
}

function renderMenuTable(items) {
    const tbody = document.getElementById('menuTableBody');
    if(!tbody) return;
    tbody.innerHTML = '';
    
    if(items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">No menu items found.</td></tr>`;
        return;
    }
    
    items.forEach(item => {
        const typeBadge = item.isVeg ? `<span class="badge-type badge-veg" style="position:static;">Veg</span>` : `<span class="badge-type badge-nonveg" style="position:static;">Non-Veg</span>`;
        const activeBadge = item.quantityAvailable > 0 ? `<span class="status-badge status-completed">● Active</span>` : `<span class="status-badge status-failed">● Out of Stock</span>`;
        
        tbody.innerHTML += `
        <tr>
            <td>
                <div style="display:flex; align-items:center; gap:12px;">
                    <img src="${item.imageUrl || 'https://placehold.co/100x100?text=Food'}" style="width:50px; height:50px; border-radius:8px; object-fit:cover;">
                    <div>
                        <div style="font-weight:600;">${item.name}</div>
                        <div style="font-size:0.8rem; color:var(--text-muted); max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${item.description}</div>
                    </div>
                </div>
            </td>
            <td><span style="background:var(--warning-bg); color:var(--warning); padding:4px 8px; border-radius:4px; font-size:0.8rem; font-weight:600;">${item.category}</span></td>
            <td>${typeBadge}</td>
            <td style="font-weight:600;">₹${item.price}</td>
            <td>${activeBadge}</td>
            <td>
                <div style="display:flex; gap:8px;">
                    <button class="btn-icon" onclick="editFood(${item.id})"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon" style="color:var(--danger); border-color:var(--danger);" onclick="deleteFood(${item.id})"><i class="fa-regular fa-trash-can"></i></button>
                </div>
            </td>
        </tr>`;
    });
}

function filterMenu() {
    const cat = document.getElementById('categoryFilter').value;
    const search = document.getElementById('searchMenu').value.toLowerCase();
    
    let filtered = allFoodItems.filter(f => {
        const matchesCat = (cat === 'All Categories' || cat === 'all') ? true : f.category === cat;
        const matchesSearch = f.name.toLowerCase().includes(search);
        return matchesCat && matchesSearch;
    });
    
    renderMenuTable(filtered);
}

function editFood(id) {
    window.location.href = `/admin/update-item.html?id=${id}`;
}

let itemToDeleteId = null;

function deleteFood(id) {
    itemToDeleteId = id;
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) modal.style.display = 'flex';
}

function hideDeleteModal() {
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) modal.style.display = 'none';
    itemToDeleteId = null;
}

async function confirmDeleteFood() {
    if (!itemToDeleteId) return;
    
    try {
        const res = await fetchWithAuth(`${API_BASE}/food/${itemToDeleteId}`, { method: 'DELETE' });
        if(res.ok) {
            showToast('Item deleted successfully');
            loadMenu();
        } else {
            showToast('Failed to delete item', 'error');
        }
    } catch(e) {
        showToast('Error deleting item', 'error');
    } finally {
        hideDeleteModal();
    }
}

// Bind modal buttons when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const btnCancel = document.getElementById('btnCancelDelete');
    const btnConfirm = document.getElementById('btnConfirmDelete');
    
    if (btnCancel) btnCancel.addEventListener('click', hideDeleteModal);
    if (btnConfirm) btnConfirm.addEventListener('click', confirmDeleteFood);
});

// Add/Update Item Logic
async function submitFoodItem(event, isUpdate = false) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    // Clean up boolean for backend
    formData.set('isVeg', formData.get('type') === 'veg');
    formData.delete('type');
    
    // Preparation time goes to description or similar if backend doesn't support it directly.
    // For now we just ignore or append. Backend has 'quantityAvailable' we should set.
    formData.set('quantityAvailable', formData.get('status') === 'Active' ? 100 : 0);
    
    try {
        let url = `${API_BASE}/food`;
        let method = 'POST';
        
        if (isUpdate) {
            const id = new URLSearchParams(window.location.search).get('id');
            url = `${API_BASE}/food/${id}`;
            method = 'PUT';
        }
        
        const res = await fetchWithAuth(url, {
            method: method,
            body: formData
        });
        
        if(res.ok) {
            showToast(isUpdate ? 'Item updated successfully!' : 'Item added successfully!');
            setTimeout(() => {
                window.location.href = '/admin/menu-management.html';
            }, 1500);
        } else {
            showToast('Failed to save item', 'error');
        }
    } catch(e) {
        showToast('Error saving item', 'error');
    }
}

// Orders Logic
async function loadOrders() {
    try {
        let url = `${API_BASE}/orders`;
        if (currentStartDate) url += `?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const res = await fetchWithAuth(url);
        if(res.ok) {
            let allOrders = await res.json();
            const orders = allOrders;
            
            // Stats
            document.getElementById('statTotalOrders').textContent = orders.length;
            document.getElementById('statPendingOrders').textContent = orders.filter(o=>o.status==='PENDING').length;
            document.getElementById('statReadyOrders').textContent = orders.filter(o=>o.status==='READY').length;
            document.getElementById('statServedOrders').textContent = orders.filter(o=>o.status==='COMPLETED').length;
            
            renderOrdersTable(orders);
        }
    } catch(e) {}
}

function renderOrdersTable(orders) {
    const tbody = document.getElementById('ordersTableBody');
    if(!tbody) return;
    tbody.innerHTML = '';
    
    if(orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No orders found.</td></tr>`;
        return;
    }
    
    orders.forEach(order => {
        const dateStr = new Date(order.orderDate).toLocaleString();
        const pickupStr = new Date(order.pickupTime).toLocaleTimeString();
        
        const statusMap = {
            'PENDING': 'status-pending',
            'PREPARING': 'status-preparing',
            'READY': 'status-ready',
            'COMPLETED': 'status-completed',
            'CANCELLED': 'status-cancelled'
        };
        const statusClass = statusMap[order.status] || 'status-pending';
        
        const paymentClass = isPaidOrder(order) ? 'status-completed' : 'status-pending';
        
        const itemsList = (order.items || []).map(i => `${i.foodItemName} (x${i.quantity})`).join(', ');
        
        tbody.innerHTML += `
        <tr>
            <td style="font-weight:600; color:var(--primary);">#CB${order.id}</td>
            <td>
                <div style="font-weight:600;">${order.studentName || order.studentEmail}</div>
            </td>
            <td style="font-size:0.85rem; color:var(--text-muted); max-width:200px;">${itemsList}</td>
            <td style="font-weight:600;">
                <div style="display:flex; align-items:center; gap:5px;"><i class="fa-regular fa-clock"></i> ${pickupStr}</div>
            </td>
            <td style="font-weight:600;">₹${order.totalAmount}</td>
            <td>
                <span class="status-badge ${paymentClass}">${order.paymentStatus}</span><br>
                <span style="font-size:0.75rem; color:var(--text-muted);">Razorpay</span>
            </td>
            <td>
                <select class="form-control" style="width:130px; padding:0.4rem; font-size:0.85rem; font-weight:600;" onchange="promptOrderStatusUpdate(${order.id}, this.value, this)">
                    <option value="PENDING" ${order.status==='PENDING'?'selected':''}>Pending</option>
                    <option value="PREPARING" ${order.status==='PREPARING'?'selected':''}>Preparing</option>
                    <option value="READY" ${order.status==='READY'?'selected':''}>Food Ready</option>
                    <option value="COMPLETED" ${order.status==='COMPLETED'?'selected':''}>Served</option>
                </select>
            </td>
            <td style="font-size:0.85rem; color:var(--text-muted);">${dateStr}</td>
        </tr>`;
    });
}

let pendingStatusUpdate = null;

function promptOrderStatusUpdate(orderId, newStatus, selectElement) {
    // Save original value to revert if cancelled
    const oldStatus = Array.from(selectElement.options).find(opt => opt.defaultSelected)?.value || 'PENDING';
    
    pendingStatusUpdate = { orderId, newStatus, selectElement, oldStatus };
    document.getElementById('statusConfirmText').textContent = `Are you sure you want to update Order #CB${orderId} to ${newStatus}?`;
    document.getElementById('statusConfirmModal').classList.add('show');
}

function closeStatusModal() {
    const modal = document.getElementById('statusConfirmModal');
    if(modal) modal.classList.remove('show');
    if(pendingStatusUpdate) {
        // Revert select visually
        pendingStatusUpdate.selectElement.value = pendingStatusUpdate.oldStatus;
        pendingStatusUpdate = null;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('confirmStatusBtn');
    if(confirmBtn) {
        confirmBtn.addEventListener('click', async () => {
            if(!pendingStatusUpdate) return;
            const { orderId, newStatus, selectElement } = pendingStatusUpdate;
            
            try {
                const res = await fetchWithAuth(`${API_BASE}/orders/${orderId}/status`, {
                    method: 'PUT',
                    body: JSON.stringify({ status: newStatus })
                });
                if(res.ok) {
                    showToast(`Order status updated to ${newStatus}`);
                    // update default selected so it doesn't revert incorrectly
                    Array.from(selectElement.options).forEach(opt => {
                        if(opt.value === newStatus) opt.defaultSelected = true;
                        else opt.defaultSelected = false;
                    });
                    
                    document.getElementById('statusConfirmModal').classList.remove('show');
                    pendingStatusUpdate = null;
                    loadOrders(); // Refresh table and stats
                } else {
                    closeStatusModal();
                    showToast('Failed to update status', 'error');
                }
            } catch(e) {
                closeStatusModal();
                showToast('Error updating status', 'error');
            }
        });
    }
});

// Payments Logic
async function loadPayments() {
    try {
        let url = `${API_BASE}/payments`;
        if (currentStartDate) url += `?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const res = await fetchWithAuth(url);
        if(res.ok) {
            const payments = await res.json();
            const tbody = document.getElementById('paymentsTableBody');
            if(!tbody) return;
            tbody.innerHTML = '';
            
            if(payments.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No successful payments found.</td></tr>`;
                return;
            }
            
            payments.forEach(pay => {
                const dateStr = new Date(pay.createdAt).toLocaleString();
                
                tbody.innerHTML += `
                <tr>
                    <td style="font-weight:600;">#PAY${pay.id}</td>
                    <td style="font-weight:600; color:var(--primary);">#CB${pay.orderId}</td>
                    <td style="font-weight:600;">${pay.studentEmail}</td>
                    <td style="font-weight:600;">₹${pay.amount}</td>
                    <td style="font-size:0.85rem; color:var(--text-muted);">${dateStr}</td>
                    <td><span class="status-badge status-completed">Paid</span></td>
                </tr>`;
            });
        }
    } catch(e) {}
}

// Image Preview Utility
function previewImage(input) {
    const preview = document.getElementById('imagePreview');
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        reader.readAsDataURL(input.files[0]);
    }
}

// Pre-fill Update Form
async function loadUpdateForm() {
    const id = new URLSearchParams(window.location.search).get('id');
    if(!id) return;
    
    try {
        const res = await fetchWithAuth(`${API_BASE}/food`); 
        if(res.ok) {
            const foods = await res.json();
            const item = foods.find(f => f.id == id);
            if(item) {
                document.getElementById('name').value = item.name;
                document.getElementById('description').value = item.description;
                document.getElementById('category').value = item.category || '';
                document.getElementById('price').value = item.price;
                const typeEl = document.getElementById('type');
                if(typeEl) typeEl.value = (item.veg || item.isVeg) ? 'veg' : 'nonveg';
                
                document.getElementById('status').value = item.quantityAvailable > 0 ? 'Active' : 'Inactive';
                
                if(item.imageUrl) {
                    const preview = document.getElementById('imagePreview');
                    preview.src = item.imageUrl;
                    preview.style.display = 'block';
                }
            }
        }
    } catch(e) {}
}

// Admin Login
async function submitAdminLogin(event) {
    event.preventDefault();
    const form = event.target;
    
    try {
        const res = await fetch(`${API_BASE}/auth/admin/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: form.username.value,
                password: form.password.value
            })
        });
        
        if (res.ok) {
            const data = await res.json();
            localStorage.setItem('adminToken', data.token);
            window.location.href = '/admin/dashboard.html';
        } else {
            showToast('Invalid credentials', 'error');
        }
    } catch(e) {
        showToast('Login failed', 'error');
    }
}


// Init
window.onload = () => {
    // Auth Check for Forms
    const path = window.location.pathname;
    
    // Attach date filter listener if present
    const df = document.getElementById('dateFilter');
    if(df) df.addEventListener('change', updateGlobalDateFilter);
    const cdf = document.getElementById('customDateFilter');
    if(cdf) cdf.addEventListener('change', updateGlobalDateFilter);
    
    if (path.includes('login')) {
        const loginForm = document.getElementById('adminLoginForm');
        if(loginForm) loginForm.addEventListener('submit', submitAdminLogin);
    } else if (path.includes('dashboard.html')) {
        loadDashboard();
    } else if (path.includes('menu-management.html')) {
        loadMenu();
    } else if (path.includes('orders.html')) {
        loadOrders();
    } else if (path.includes('payments.html')) {
        loadPayments();
    } else if (path.includes('update-item.html')) {
        loadUpdateForm();
        const f = document.getElementById('updateFoodForm');
        if(f) f.addEventListener('submit', (e) => submitFoodItem(e, true));
    } else if (path.includes('add-item.html')) {
        const f = document.getElementById('addFoodForm');
        if(f) f.addEventListener('submit', (e) => submitFoodItem(e, false));
    }
};
