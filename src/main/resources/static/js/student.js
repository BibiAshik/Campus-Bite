// Uses API_BASE and toggleNav from common.js
// Utility for fetching with Auth Token
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('studentToken');
    if (!token) {
        window.location.href = '/student/login';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };

    try {
        const response = await fetch(url, { ...options, headers });
        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('studentToken');
            window.location.href = '/student/login';
            return;
        }
        return response;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Extract token from URL if present (after OAuth redirect)
const urlParams = new URLSearchParams(window.location.search);
const tokenFromUrl = urlParams.get('token');
if (tokenFromUrl) {
    localStorage.setItem('studentToken', tokenFromUrl);
    window.history.replaceState({}, document.title, window.location.pathname);
}

// Check auth for protected pages
if (window.location.pathname.includes('/student/') && !window.location.pathname.includes('/login')) {
    if (!localStorage.getItem('studentToken')) {
        window.location.href = '/student/login';
    }
}

// Reusable Toast Component
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? '<i class="fa-solid fa-circle-check" style="color:var(--success)"></i>' : '<i class="fa-solid fa-circle-xmark" style="color:var(--danger)"></i>';
    toast.innerHTML = `${icon} <span>${message}</span>`;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Navbar Initialization
async function loadNavbarData() {
    try {
        // Load Profile
        const profileRes = await fetchWithAuth(`${API_BASE}/student/profile`);
        if (profileRes && profileRes.ok) {
            const profile = await profileRes.json();
            const nameEl = document.getElementById('navProfileName');
            const picEl = document.getElementById('navProfilePic');
            if (nameEl) nameEl.innerHTML = `Hi, ${profile.name.split(' ')[0]} 👋`;
            if (picEl && profile.profileImageUrl) picEl.src = profile.profileImageUrl;
            
            // Populate profile page if we are on it
            if(window.location.pathname.includes('/profile')) {
                document.getElementById('profileName').textContent = profile.name;
                document.getElementById('profileEmail').textContent = profile.email;
                document.getElementById('profilePhone').textContent = profile.phone || "Not Provided";
                if(profile.profileImageUrl) document.getElementById('mainProfilePic').src = profile.profileImageUrl;
            }
        }
        
        loadCartData(); // Also loads cart count
    } catch(err) {}
}

let cartItems = [];
async function loadCartData() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/cart`);
        if (res && res.ok) {
            cartItems = await res.json();
            updateCartUI();
        }
    } catch(e) {}
}

function updateCartUI() {
    const count = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    
    // Update Navbar Badge
    const navBadge = document.getElementById('navCartCount');
    if(navBadge) navBadge.textContent = count;
    
    // Subtotal calculations
    const subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const gst = subtotal * 0.05;
    const total = subtotal + gst;
    
    // Update Quick Sidebar if it exists
    if(document.getElementById('quickCartCount')) {
        document.getElementById('quickCartCount').textContent = count;
        document.getElementById('quickCartSubtotal').textContent = `₹${subtotal.toFixed(2)}`;
        document.getElementById('quickCartGst').textContent = `₹${gst.toFixed(2)}`;
        document.getElementById('quickCartTotal').textContent = `₹${total.toFixed(2)}`;
        
        const quickContainer = document.getElementById('quickCartItems');
        quickContainer.innerHTML = '';
        if(cartItems.length === 0) {
            quickContainer.innerHTML = `<p style="color: var(--text-muted); text-align: center; padding: 1rem 0;">Your cart is empty.</p>`;
        } else {
            cartItems.forEach(item => {
                quickContainer.innerHTML += `
                <div style="display:flex; justify-content:space-between; margin-bottom:10px; border-bottom: 1px solid var(--border-color); padding-bottom: 10px;">
                    <div>
                        <div style="font-weight: 500; font-size:0.9rem;">${item.name}</div>
                        <div style="font-size:0.8rem; color:var(--text-muted);">${item.quantity} x ₹${item.price}</div>
                    </div>
                    <div style="font-weight: 600;">₹${item.totalItemPrice}</div>
                </div>`;
            });
        }
    }
    
    // Update Cart Page specific UI
    if(window.location.pathname.includes('/cart') || window.location.pathname.includes('/checkout')) {
        const titleCount = document.getElementById('cartTitleCount');
        if(titleCount) titleCount.textContent = `${count} Items`;
        
        const summaryCount = document.getElementById('summaryCount');
        if(summaryCount) summaryCount.textContent = count;
        
        document.getElementById('cartSubtotal').textContent = `₹${subtotal.toFixed(2)}`;
        document.getElementById('cartGst').textContent = `₹${gst.toFixed(2)}`;
        document.getElementById('cartTotal').textContent = `₹${total.toFixed(2)}`;
        
        const cartContainer = document.getElementById('cartItemsContainer') || document.getElementById('checkoutItemsList');
        cartContainer.innerHTML = '';
        
        if(cartItems.length === 0) {
            cartContainer.innerHTML = `
            <div class="card" style="text-align:center; padding: 3rem;">
                <i class="fa-solid fa-cart-shopping" style="font-size: 3rem; color: var(--border-color); margin-bottom:1rem;"></i>
                <h3 style="margin-bottom: 1rem;">Your cart is empty</h3>
                <a href="/student/menu" class="btn btn-primary">Browse Menu</a>
            </div>`;
        } else {
            const isCheckout = window.location.pathname.includes('/checkout');
            cartItems.forEach(item => {
                const vegIcon = `<span style="background:var(--warning-bg); color:var(--warning); padding:2px 6px; border-radius:4px; font-size:0.75rem; font-weight:600;">${item.category || 'Food'}</span>`;
                const controls = isCheckout ? `<div style="font-weight: 600;">Qty: ${item.quantity}</div><div style="font-weight:700; color:var(--primary); margin-left: 1rem;">₹${item.totalItemPrice}</div>` : 
                `<div style="display:flex; align-items:center; gap:1.5rem;">
                    <div class="cart-qty-controls">
                        <button class="cart-qty-btn" onclick="updateCartItem(${item.id}, ${item.quantity - 1})">-</button>
                        <span style="font-weight: 600; width: 20px; text-align: center;">${item.quantity}</span>
                        <button class="cart-qty-btn" onclick="updateCartItem(${item.id}, ${item.quantity + 1})">+</button>
                    </div>
                    <div style="font-weight: 700; font-size:1.1rem; width: 60px;">₹${item.totalItemPrice}</div>
                    <button class="btn-icon" style="color:var(--danger); border-color:var(--danger);" onclick="removeCartItem(${item.id})"><i class="fa-regular fa-trash-can"></i></button>
                </div>`;

                cartContainer.innerHTML += `
                <div class="cart-item">
                    <div style="display:flex; align-items:center; gap: 1rem;">
                        <img src="${item.imageUrl || 'https://placehold.co/100x100?text=Food'}" style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px;">
                        <div>
                            <h4 style="font-size: 1.1rem; margin-bottom: 4px;">${item.name}</h4>
                            <div class="type-indicator" style="margin-bottom: 4px;">${vegIcon}</div>
                            <div style="font-size: 0.9rem; color: var(--primary); font-weight: 600;">₹${item.price}</div>
                        </div>
                    </div>
                    ${controls}
                </div>`;
            });
        }
    }
}

async function addToCart(foodId) {
    try {
        const res = await fetchWithAuth(`${API_BASE}/cart`, {
            method: 'POST',
            body: JSON.stringify({ foodItemId: foodId, quantity: 1 })
        });
        if(res.ok) {
            showToast('Added to cart successfully');
            loadCartData();
        }
    } catch(e) {
        showToast('Failed to add to cart', 'error');
    }
}

async function updateCartItem(cartItemId, newQty) {
    try {
        const res = await fetchWithAuth(`${API_BASE}/cart/${cartItemId}?quantity=${newQty}`, { method: 'PUT' });
        if(res.ok) {
            loadCartData();
        }
    } catch(e) {}
}

async function removeCartItem(cartItemId) {
    try {
        const res = await fetchWithAuth(`${API_BASE}/cart/${cartItemId}`, { method: 'DELETE' });
        if(res.ok) {
            showToast('Item removed from cart');
            loadCartData();
        }
    } catch(e) {}
}

// Menu Page Logic
let allFoodsData = [];

async function loadMenu() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/food`);
        if (res.ok) {
            allFoodsData = await res.json();
            renderMenuGrid(allFoodsData);
        }
    } catch(e) {}
}

function renderMenuGrid(foods) {
    const grid = document.getElementById('menuGrid');
    if(!grid) return;
    
    grid.innerHTML = '';
    if(foods.length === 0) {
        grid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; padding: 2rem;">No items found.</p>`;
        return;
    }
    
    foods.forEach(item => {
        const isSoldOut = item.quantityAvailable != null && item.quantityAvailable <= 0;
        
        const cardStyle = isSoldOut ? 'opacity: 0.6; pointer-events: none;' : '';
        const overlayHTML = isSoldOut ? `
            <div style="position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.6); display:flex; align-items:center; justify-content:center; border-radius: 12px 12px 0 0;">
                <span style="color: white; font-weight: 800; font-size: 1.5rem; letter-spacing: 2px; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); transform: rotate(-10deg); border: 3px solid white; padding: 5px 15px;">SOLD OUT</span>
            </div>
        ` : '';
        
        const buttonHTML = isSoldOut 
            ? `<button class="btn btn-outline" style="width:100%; background: #e5e7eb; color: #9ca3af; border-color: #e5e7eb; cursor: not-allowed; pointer-events: auto;" disabled>Unavailable</button>`
            : `<button class="btn btn-outline" style="width:100%;" onclick="addToCart(${item.id})">Add to Cart <i class="fa-solid fa-plus"></i></button>`;

        grid.innerHTML += `
        <div class="card" style="${cardStyle}">
            <div style="position: relative;">
                <img src="${item.imageUrl || 'https://placehold.co/300x200?text=Food'}" class="card-img" alt="${item.name}">
                ${overlayHTML}
            </div>
            <div class="card-body">
                <h3 class="card-title">${item.name}</h3>
                <p class="card-desc">${item.description}</p>
                <div class="card-flex">
                    <div class="card-price">₹${item.price}</div>
                </div>
                <div style="margin-top: 1rem; display: flex; gap: 10px;">
                    ${buttonHTML}
                </div>
            </div>
        </div>`;
    });
}

// Checkout Logic
async function placeOrder() {
    const time = document.getElementById('checkoutPickupTime').value;
    if(!time) {
        showToast('Please select a pickup time', 'error');
        return;
    }

    try {
        const btn = document.getElementById('btnPayNow');
        const originalText = btn.innerHTML;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
        btn.disabled = true;

        // 1. Create Order in backend
        const orderRes = await fetchWithAuth(`${API_BASE}/orders`, {
            method: 'POST',
            body: JSON.stringify({
                pickupTime: new Date().toISOString(),
                items: cartItems.map(i => ({ foodItemId: i.foodItemId, quantity: i.quantity }))
            })
        });

        if(!orderRes.ok) {
            btn.innerHTML = originalText;
            btn.disabled = false;
            
            try {
                const errorData = await orderRes.json();
                if(errorData.message && (errorData.message.includes('Sorry,') || errorData.message.includes('Not enough'))) {
                    document.getElementById('errorModalMessage').textContent = errorData.message;
                    document.getElementById('errorModal').style.display = 'flex';
                    return;
                }
            } catch(e) {}

            showToast('Failed to create order', 'error');
            return;
        }
        
        const orderData = await orderRes.json();
        
        // 2. Create Razorpay order
        const rzpRes = await fetchWithAuth(`${API_BASE}/payments/create`, {
            method: 'POST',
            body: JSON.stringify({ orderId: orderData.id })
        });
        
        if(!rzpRes.ok) {
            btn.innerHTML = originalText;
            btn.disabled = false;
            showToast('Failed to initialize payment gateway', 'error');
            return;
        }

        const rzpData = await rzpRes.json();

        // 3. Open Razorpay Checkout
        var options = {
            "key": "rzp_test_T4fJ5573E5tC0t", // Same as in application.properties
            "amount": rzpData.amount * 100, // paise
            "currency": "INR",
            "name": "CampusBite",
            "description": "Food Order Payment",
            "order_id": rzpData.razorpayOrderId, // Use Razorpay's order ID, not internal DB ID
            "handler": async function (response){
                // 4. Verify Payment on success
                try {
                    const verifyRes = await fetchWithAuth(`${API_BASE}/payments/verify`, {
                        method: 'POST',
                        body: JSON.stringify({
                            razorpayOrderId: response.razorpay_order_id,
                            razorpayPaymentId: response.razorpay_payment_id,
                            razorpaySignature: response.razorpay_signature
                        })
                    });
                    
                    if(verifyRes.ok) {
                        // Clear Cart
                        await fetchWithAuth(`${API_BASE}/cart`, { method: 'DELETE' });
                        
                        // Show Custom Success Modal
                        const modal = document.getElementById('orderSuccessModal');
                        document.getElementById('successOrderId').textContent = '#' + orderData.tokenNumber;
                        modal.classList.add('show');
                    } else {
                        showToast('Payment verification failed!', 'error');
                    }
                } catch(e) {
                    showToast('Error during verification', 'error');
                }
            },
            "prefill": {
                "name": "Student",
                "email": "student@sairamtap.edu.in",
            },
            "theme": {
                "color": "#f97316"
            }
        };
        
        var rzp1 = new Razorpay(options);
        rzp1.on('payment.failed', function (response){
            showToast('Payment Failed: ' + response.error.description, 'error');
        });
        
        rzp1.open();
        
        // Reset button
        btn.innerHTML = originalText;
        btn.disabled = false;
        
    } catch(e) {
        showToast('Something went wrong', 'error');
        const btn = document.getElementById('btnPayNow');
        if(btn) {
            btn.innerHTML = 'Pay & Place Order <i class="fa-solid fa-lock"></i>';
            btn.disabled = false;
        }
    }
}

// Orders Logic
let currentOrderStatus = 'ALL';
let currentOrderStartDate = '';
let currentOrderEndDate = '';

let currentOrderPage = 1;
const ORDERS_PER_PAGE = 5;

async function loadOrders() {
    try {
        let url = `${API_BASE}/orders/my-orders?status=${currentOrderStatus}`;
        if (currentOrderStartDate && currentOrderEndDate) {
            url += `&startDate=${currentOrderStartDate}&endDate=${currentOrderEndDate}`;
        }

        const res = await fetchWithAuth(url);
        if(res.ok) {
            let allOrders = await res.json();
            // Filter to only show successfully paid orders
            const orders = allOrders.filter(o => o.paymentStatus === 'PAID');
            
            // Update sidebar counts
            if (currentOrderStatus === 'ALL' && !currentOrderStartDate) {
                document.getElementById('countAll').textContent = orders.length;
                document.getElementById('countPending').textContent = orders.filter(o => o.status === 'PENDING').length;
                document.getElementById('countPreparing').textContent = orders.filter(o => o.status === 'PREPARING').length;
                document.getElementById('countReady').textContent = orders.filter(o => o.status === 'READY').length;
                document.getElementById('countCompleted').textContent = orders.filter(o => o.status === 'COMPLETED').length;
            }

            const container = document.getElementById('ordersContainer');
            if(!container) return;
            container.innerHTML = '';
            
            if(orders.length === 0) {
                container.innerHTML = `<p style="text-align: center; padding: 2rem;">You have no orders matching these filters.</p>`;
                document.getElementById('orderPagination').innerHTML = '';
                return;
            }

            // Pagination logic
            const totalPages = Math.ceil(orders.length / ORDERS_PER_PAGE);
            if (currentOrderPage > totalPages) currentOrderPage = totalPages;
            
            const startIndex = (currentOrderPage - 1) * ORDERS_PER_PAGE;
            const paginatedOrders = orders.slice(startIndex, startIndex + ORDERS_PER_PAGE);

            paginatedOrders.forEach(order => {
                const date = new Date(order.orderDate).toLocaleString();
                
                let itemsHtml = order.items.map(i => `<li><span>• ${i.foodItemName}</span> <span>x${i.quantity}</span></li>`).join('');

                container.innerHTML += `
                <div class="order-card">
                    <img src="https://placehold.co/140x140?text=Order" class="order-card-img">
                    <div class="order-card-main">
                        <div>
                            <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 4px;">Order ID</div>
                            <h3 style="font-family: 'Space Grotesk', sans-serif; font-size: 1.25rem; margin-bottom: 1rem;">#CB${order.id}</h3>
                            <div style="display: flex; gap: 8px; color: var(--text-muted); font-size: 0.85rem;">
                                <i class="fa-regular fa-clock"></i> Pickup Time <br>
                                <span style="font-weight: 600; color: var(--text-main);">${new Date(order.pickupTime).toLocaleTimeString()}</span>
                            </div>
                        </div>
                        <div>
                            <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 8px;">Items</div>
                            <ul class="order-items-list">${itemsHtml}</ul>
                            <div style="margin-top: 10px; font-size: 0.85rem; font-weight: 500;">Total Items: ${order.items.reduce((s,i)=>s+i.quantity,0)}</div>
                        </div>
                    </div>
                    <div class="order-card-right">
                        <div style="width: 100%;">
                            <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 4px;">Payment Status</div>
                            <div style="color: var(--success); font-weight: 600; margin-bottom: 5px;"><i class="fa-solid fa-check"></i> Paid</div>
                            <div style="font-family: 'Space Grotesk', sans-serif; font-size: 1.5rem; font-weight: 700; color: var(--primary); margin-bottom: 15px;">₹${order.totalAmount}</div>
                            <button class="btn btn-outline" style="width: 100%; padding: 0.5rem;" onclick="viewOrderDetails(${order.id})">View Details</button>
                        </div>
                    </div>
                </div>`;
            });
            
            renderOrderPagination(totalPages);
        }
    } catch(e) {}
}

function renderOrderPagination(totalPages) {
    const paginationContainer = document.getElementById('orderPagination');
    if (!paginationContainer) return;
    
    if (totalPages <= 1) {
        paginationContainer.innerHTML = '';
        return;
    }
    
    let html = '';
    
    if (currentOrderPage > 1) {
        html += `<button class="btn btn-icon" onclick="changeOrderPage(${currentOrderPage - 1})"><i class="fa-solid fa-chevron-left"></i></button>`;
    }
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentOrderPage) {
            html += `<button class="btn btn-icon" style="background: var(--primary); color: white; border-color: var(--primary);">${i}</button>`;
        } else {
            html += `<button class="btn btn-icon" onclick="changeOrderPage(${i})">${i}</button>`;
        }
    }
    
    if (currentOrderPage < totalPages) {
        html += `<button class="btn btn-icon" onclick="changeOrderPage(${currentOrderPage + 1})"><i class="fa-solid fa-chevron-right"></i></button>`;
    }
    
    paginationContainer.innerHTML = html;
}

function changeOrderPage(page) {
    currentOrderPage = page;
    loadOrders();
}

// Hook up my-orders page filters
document.addEventListener('DOMContentLoaded', () => {
    // Status Buttons
    const statusBtns = document.querySelectorAll('.filter-status-btn');
    if (statusBtns.length > 0) {
        statusBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                statusBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentOrderStatus = btn.getAttribute('data-status');
                loadOrders();
            });
        });
    }

    const dateFilter = document.getElementById('dateFilter');
    if (dateFilter) {
        dateFilter.addEventListener('change', (e) => {
            const val = e.target.value;
            const formatLocalDateTime = (date) => date.toISOString().substring(0, 19);
            
            if (val === 'today') {
                const start = new Date(); start.setHours(0,0,0,0);
                const end = new Date(); end.setHours(23,59,59,999);
                currentOrderStartDate = formatLocalDateTime(start);
                currentOrderEndDate = formatLocalDateTime(end);
            } else if (val === 'week') {
                const start = new Date();
                start.setDate(start.getDate() - start.getDay()); // Sunday
                start.setHours(0,0,0,0);
                const end = new Date(); end.setHours(23,59,59,999);
                currentOrderStartDate = formatLocalDateTime(start);
                currentOrderEndDate = formatLocalDateTime(end);
            } else {
                currentOrderStartDate = '';
                currentOrderEndDate = '';
            }
            loadOrders();
        });
    }
});

function viewOrderDetails(orderId) {
    const modal = document.getElementById('orderModal');
    if(modal) {
        document.getElementById('orderDetailsBody').innerHTML = `
            <div style="text-align: center; padding: 3rem 1rem; height: 150px; display: flex; align-items: center; justify-content: center;">
                <h2 style="font-family: 'Space Grotesk', sans-serif; font-size: 2.5rem; color: var(--primary); margin: 0;">#CB${orderId}</h2>
            </div>
        `;
        modal.classList.add('show');
    }
}

// Payments Logic
let currentPaymentSearch = '';
let currentPaymentStartDate = '';
let currentPaymentEndDate = '';

let currentPaymentPage = 1;
const PAYMENTS_PER_PAGE = 5;

async function loadPayments() {
    try {
        let url = `${API_BASE}/payments/my-payments?1=1`;
        if (currentPaymentSearch) url += `&searchId=${encodeURIComponent(currentPaymentSearch)}`;
        if (currentPaymentStartDate && currentPaymentEndDate) {
            url += `&startDate=${currentPaymentStartDate}&endDate=${currentPaymentEndDate}`;
        }

        const res = await fetchWithAuth(url);
        if(res.ok) {
            const payments = await res.json();
            const tbody = document.getElementById('paymentsTableBody');
            if(!tbody) return;
            tbody.innerHTML = '';
            
            if(payments.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">No payments found.</td></tr>`;
                document.getElementById('paymentPagination').innerHTML = '';
                return;
            }

            // Pagination logic
            const totalPages = Math.ceil(payments.length / PAYMENTS_PER_PAGE);
            if (currentPaymentPage > totalPages) currentPaymentPage = totalPages;
            
            const startIndex = (currentPaymentPage - 1) * PAYMENTS_PER_PAGE;
            const paginatedPayments = payments.slice(startIndex, startIndex + PAYMENTS_PER_PAGE);

            paginatedPayments.forEach(payment => {
                const date = new Date(payment.createdAt).toLocaleString();
                
                // Show FAILED instead of PENDING
                const displayStatus = payment.status === 'SUCCESS' ? 'PAID' : (payment.status === 'PENDING' ? 'FAILED' : payment.status);
                const statusClass = displayStatus === 'PAID' ? 'status-completed' : (displayStatus === 'FAILED' ? 'status-failed' : 'status-pending');
                
                tbody.innerHTML += `
                <tr>
                    <td style="font-weight: 500;">#PAY${payment.id}</td>
                    <td style="color: var(--primary); font-weight: 600;">#CB${payment.orderId}</td>
                    <td>${date}</td>
                    <td style="font-weight: 600;">₹${payment.amount}</td>
                    <td><span class="status-badge ${statusClass}">${displayStatus}</span></td>
                </tr>`;
            });
            
            renderPaymentPagination(totalPages);
        }
    } catch(e) {}
}

function renderPaymentPagination(totalPages) {
    const paginationContainer = document.getElementById('paymentPagination');
    if (!paginationContainer) return;
    
    if (totalPages <= 1) {
        paginationContainer.innerHTML = '';
        return;
    }
    
    let html = '';
    
    if (currentPaymentPage > 1) {
        html += `<button class="btn btn-icon" onclick="changePaymentPage(${currentPaymentPage - 1})"><i class="fa-solid fa-chevron-left"></i></button>`;
    }
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPaymentPage) {
            html += `<button class="btn btn-icon" style="background: var(--primary); color: white; border-color: var(--primary);">${i}</button>`;
        } else {
            html += `<button class="btn btn-icon" onclick="changePaymentPage(${i})">${i}</button>`;
        }
    }
    
    if (currentPaymentPage < totalPages) {
        html += `<button class="btn btn-icon" onclick="changePaymentPage(${currentPaymentPage + 1})"><i class="fa-solid fa-chevron-right"></i></button>`;
    }
    
    paginationContainer.innerHTML = html;
}

function changePaymentPage(page) {
    currentPaymentPage = page;
    loadPayments();
}

document.addEventListener('DOMContentLoaded', () => {
    // Payment Search
    const searchPayments = document.getElementById('searchPayments');
    if (searchPayments) {
        searchPayments.addEventListener('input', (e) => {
            currentPaymentSearch = e.target.value;
            loadPayments();
        });
    }

    // Payment Date Filter
    const paymentDateFilter = document.getElementById('paymentDateFilter');
    if (paymentDateFilter) {
        paymentDateFilter.addEventListener('change', (e) => {
            const val = e.target.value;
            const formatLocalDateTime = (date) => date.toISOString().substring(0, 19);
            
            if (val === 'today') {
                const start = new Date(); start.setHours(0,0,0,0);
                const end = new Date(); end.setHours(23,59,59,999);
                currentPaymentStartDate = formatLocalDateTime(start);
                currentPaymentEndDate = formatLocalDateTime(end);
            } else if (val === 'week') {
                const start = new Date();
                start.setDate(start.getDate() - start.getDay());
                start.setHours(0,0,0,0);
                const end = new Date(); end.setHours(23,59,59,999);
                currentPaymentStartDate = formatLocalDateTime(start);
                currentPaymentEndDate = formatLocalDateTime(end);
            } else {
                currentPaymentStartDate = '';
                currentPaymentEndDate = '';
            }
            loadPayments();
        });
    }
});

// Init Page
// Init Page
document.addEventListener('DOMContentLoaded', () => {
    loadNavbarData();
    
    if(window.location.pathname.includes('/menu')) {
        loadMenu();
        
        // Menu Filters Logic
        const filterBtns = document.querySelectorAll('.filter-btn');
        if (filterBtns.length > 0) {
            filterBtns.forEach(btn => {
                btn.addEventListener('click', () => {
                    filterBtns.forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                    
                    const type = btn.getAttribute('data-type');
                    if (type === 'all') {
                        renderMenuGrid(allFoodsData);
                    } else {
                        renderMenuGrid(allFoodsData.filter(f => f.category && f.category.toLowerCase() === type));
                    }
                });
            });
        }
        
    } else if(window.location.pathname.includes('/my-orders')) {
        loadOrders();
    } else if(window.location.pathname.includes('/my-payments')) {
        loadPayments();
    }
    
    const btnProceed = document.getElementById('btnProceedCheckout');
    if(btnProceed) {
        btnProceed.addEventListener('click', () => { window.location.href = '/student/checkout'; });
    }
    
    const btnPay = document.getElementById('btnPayNow');
    if(btnPay) {
        btnPay.addEventListener('click', placeOrder);
    }
});

function closeErrorModal() {
    document.getElementById('errorModal').style.display = 'none';
    window.location.href = '/student/menu';
}


