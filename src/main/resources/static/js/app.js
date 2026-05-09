let menuItems = [];
let cart = JSON.parse(localStorage.getItem('campusbite_cart')) || [];

document.addEventListener("DOMContentLoaded", () => {
    fetchMenu();
    updateCartUI();
});

const fallbackMenuData = [
    { id: 1, name: "Classic Burger", category: "Non-Veg", price: 120.0, imageUrl: "burger.jpg", quantityAvailable: 50, isVeg: false, description: "Juicy chicken patty with fresh lettuce and cheese." },
    { id: 2, name: "Veggie Sandwich", category: "Veg", price: 60.0, imageUrl: "sandwich.jpg", quantityAvailable: 30, isVeg: true, description: "Healthy sandwich with fresh cucumber, tomato, and mayo." },
    { id: 3, name: "Chicken Fried Rice", category: "Non-Veg", price: 150.0, imageUrl: "fried-rice.jpg", quantityAvailable: 40, isVeg: false, description: "Wok-tossed rice with chicken chunks and veggies." },
    { id: 4, name: "Chicken Shawarma", category: "Non-Veg", price: 100.0, imageUrl: "shawarma.jpg", quantityAvailable: 60, isVeg: false, description: "Authentic Arabic style chicken shawarma wrap." },
    { id: 5, name: "French Fries", category: "Veg", price: 80.0, imageUrl: "fries.jpg", quantityAvailable: 100, isVeg: true, description: "Crispy golden french fries with peri-peri seasoning." },
    { id: 6, name: "Student Combo Meal", category: "Combos", price: 200.0, imageUrl: "combo-meal.jpg", quantityAvailable: 20, isVeg: false, description: "Burger, Fries, and a Coke." },
    { id: 7, name: "Coca Cola", category: "Beverages", price: 40.0, imageUrl: "coke.jpg", quantityAvailable: 100, isVeg: true, description: "Chilled Coca Cola 300ml." },
    { id: 8, name: "Fresh Lemon Juice", category: "Beverages", price: 30.0, imageUrl: "lemon-juice.jpg", quantityAvailable: 50, isVeg: true, description: "Freshly squeezed sweet and salt lemon juice." },
    { id: 9, name: "Veg Noodles", category: "Veg", price: 110.0, imageUrl: "noodles.jpg", quantityAvailable: 40, isVeg: true, description: "Hakka style veg noodles with spring onions." }
];

async function fetchMenu() {
    try {
        const response = await fetch('/api/food');
        if (!response.ok) throw new Error("API not responding");
        menuItems = await response.json();
        renderMenu(menuItems);
    } catch (error) {
        console.warn('Backend API unavailable. Loading static fallback data for UI preview.');
        menuItems = fallbackMenuData;
        renderMenu(menuItems);
    }
}

function renderMenu(items) {
    const grid = document.getElementById('menu-grid');
    grid.innerHTML = '';

    if (items.length === 0) {
        grid.innerHTML = '<p>No items found.</p>';
        return;
    }

    items.forEach(item => {
        const isSoldOut = item.quantityAvailable <= 0;
        
        const card = document.createElement('div');
        card.className = 'food-card';
        card.innerHTML = `
            <img src="images/${item.imageUrl || 'placeholder.jpg'}" alt="${item.name}" class="food-image" onerror="this.src='https://via.placeholder.com/250x180?text=Food+Image'">
            <div class="food-info">
                <div class="food-header">
                    <span class="food-name">${item.name}</span>
                    <span class="badge ${item.isVeg ? 'veg' : 'non-veg'}">${item.isVeg ? 'Veg' : 'Non-Veg'}</span>
                </div>
                <div class="food-desc">${item.description || ''}</div>
                <div class="food-price">₹${item.price.toFixed(2)}</div>
                <p style="font-size:0.8rem; color:#666; margin-bottom: 8px;">Available: ${item.quantityAvailable}</p>
                <button class="add-to-cart-btn" onclick="addToCart(${item.id})" ${isSoldOut ? 'disabled' : ''}>
                    ${isSoldOut ? 'SOLD OUT' : 'Add to Cart'}
                </button>
            </div>
        `;
        grid.appendChild(card);
    });
}

function filterMenu(category) {
    // Update active button
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    // Filter items
    if (category === 'All') {
        renderMenu(menuItems);
    } else {
        const filtered = menuItems.filter(item => item.category === category);
        renderMenu(filtered);
    }
}

function searchMenu() {
    const query = document.getElementById('search-input').value.toLowerCase();
    const filtered = menuItems.filter(item => item.name.toLowerCase().includes(query));
    renderMenu(filtered);
}

// Cart Logic
function addToCart(itemId) {
    const item = menuItems.find(i => i.id === itemId);
    if (!item) return;

    const existingItem = cart.find(i => i.foodItemId === itemId);
    if (existingItem) {
        if (existingItem.quantity < item.quantityAvailable) {
            existingItem.quantity++;
        } else {
            alert('Cannot add more. Reached max available quantity.');
        }
    } else {
        cart.push({
            foodItemId: item.id,
            name: item.name,
            price: item.price,
            quantity: 1
        });
    }

    saveCart();
    updateCartUI();
    
    // Simple visual feedback
    const btn = event.target;
    const originalText = btn.innerText;
    btn.innerText = 'Added!';
    setTimeout(() => btn.innerText = originalText, 1000);
}

function updateCartQuantity(itemId, delta) {
    const itemIndex = cart.findIndex(i => i.foodItemId === itemId);
    if (itemIndex !== -1) {
        const menuItem = menuItems.find(i => i.id === itemId);
        
        cart[itemIndex].quantity += delta;
        
        if (cart[itemIndex].quantity <= 0) {
            cart.splice(itemIndex, 1);
        } else if (menuItem && cart[itemIndex].quantity > menuItem.quantityAvailable) {
            cart[itemIndex].quantity = menuItem.quantityAvailable;
            alert('Max quantity reached.');
        }
        
        saveCart();
        updateCartUI();
    }
}

function saveCart() {
    localStorage.setItem('campusbite_cart', JSON.stringify(cart));
}

function updateCartUI() {
    // Update count in navbar
    const count = cart.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cart-count').innerText = count;

    // Render cart items in modal
    const container = document.getElementById('cart-items-container');
    if (!container) return; // Not on homepage

    container.innerHTML = '';
    
    if (cart.length === 0) {
        container.innerHTML = '<p>Your cart is empty.</p>';
        document.getElementById('cart-total-amount').innerText = '₹0.00';
        document.getElementById('checkout-btn').disabled = true;
        return;
    }

    let total = 0;
    
    cart.forEach(item => {
        total += item.price * item.quantity;
        const div = document.createElement('div');
        div.className = 'cart-item';
        div.innerHTML = `
            <div class="cart-item-info">
                <strong>${item.name}</strong><br>
                <span>₹${item.price.toFixed(2)}</span>
            </div>
            <div class="cart-item-controls">
                <button class="qty-btn" type="button" onclick="updateCartQuantity(${item.foodItemId}, -1)">-</button>
                <span>${item.quantity}</span>
                <button class="qty-btn" type="button" onclick="updateCartQuantity(${item.foodItemId}, 1)">+</button>
            </div>
        `;
        container.appendChild(div);
    });

    document.getElementById('cart-total-amount').innerText = `₹${total.toFixed(2)}`;
    document.getElementById('checkout-btn').disabled = false;
}

function openCart() {
    document.getElementById('cart-modal').classList.add('active');
}

function closeCart() {
    document.getElementById('cart-modal').classList.remove('active');
}

async function placeOrder(event) {
    event.preventDefault();
    if (cart.length === 0) return;

    const studentName = document.getElementById('student-name').value;
    const rollNumber = document.getElementById('roll-number').value;
    const pickupTime = document.getElementById('pickup-time').value;

    const orderRequest = {
        studentName,
        rollNumber,
        pickupTime,
        items: cart.map(item => ({
            foodItemId: item.foodItemId,
            quantity: item.quantity
        }))
    };

    try {
        document.getElementById('checkout-btn').innerText = 'Processing...';
        document.getElementById('checkout-btn').disabled = true;

        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderRequest)
        });

        const result = await response.json();

        if (response.ok) {
            // Clear cart
            cart = [];
            saveCart();
            
            // Redirect to success page with token
            window.location.href = `order-success.html?token=${result.token}`;
        } else {
            alert(result.error || 'Failed to place order.');
            document.getElementById('checkout-btn').innerText = 'Place Order';
            document.getElementById('checkout-btn').disabled = false;
        }
    } catch (error) {
        alert('An error occurred. Please try again.');
        document.getElementById('checkout-btn').innerText = 'Place Order';
        document.getElementById('checkout-btn').disabled = false;
    }
}
