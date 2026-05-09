async function fetchOrders() {
    try {
        const response = await fetch('/api/orders');
        if (response.status === 401 || response.status === 403) {
            window.location.href = 'admin-login.html';
            return;
        }
        
        const orders = await response.json();
        renderOrders(orders);
        updateStats(orders);
    } catch (error) {
        console.error('Failed to fetch orders', error);
    }
}

function renderOrders(orders) {
    const tbody = document.querySelector('#orders-table tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No orders found.</td></tr>';
        return;
    }

    orders.forEach(order => {
        const itemsStr = order.items.map(i => `${i.quantity}x ${i.foodItem.name}`).join(', ');
        const date = new Date(order.orderDate).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${order.tokenNumber}</strong></td>
            <td>${order.studentName}</td>
            <td>${order.rollNumber}</td>
            <td style="max-width: 250px; font-size: 0.9rem;">${itemsStr}</td>
            <td>${date}</td>
            <td><span class="status-badge status-${order.status}">${order.status}</span></td>
            <td>
                ${order.status === 'PENDING' ? `<button class="action-btn btn-info" onclick="updateStatus(${order.id}, 'READY')">Mark Ready</button>` : ''}
                ${order.status === 'READY' ? `<button class="action-btn btn-success" onclick="updateStatus(${order.id}, 'PICKED_UP')">Complete</button>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateStats(orders) {
    const pendingCount = orders.filter(o => o.status === 'PENDING').length;
    const totalCount = orders.length;
    const revenue = orders.reduce((sum, o) => sum + o.totalAmount, 0);

    const elPending = document.getElementById('stat-pending');
    const elTotal = document.getElementById('stat-total');
    const elRev = document.getElementById('stat-revenue');

    if (elPending) elPending.innerText = pendingCount;
    if (elTotal) elTotal.innerText = totalCount;
    if (elRev) elRev.innerText = `₹${revenue.toFixed(2)}`;
}

async function updateStatus(orderId, newStatus) {
    try {
        const response = await fetch(`/api/orders/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });
        
        if (response.ok) {
            fetchOrders(); // refresh table
        } else {
            alert('Failed to update status');
        }
    } catch (error) {
        console.error(error);
    }
}
