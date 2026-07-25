/**
 * Common Utilities (Used across Admin and Student portals)
 */

const API_BASE = '/api';

/**
 * Basic UI Utilities
 */
function toggleNav() {
    const navLinks = document.querySelector('.nav-links');
    const overlay = document.querySelector('.nav-overlay');
    
    if (navLinks) navLinks.classList.toggle('show');
    if (overlay) overlay.classList.toggle('show');
}

/**
 * Reusable Toast Component
 */
function showToast(message, type = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.style.position = 'fixed';
        container.style.bottom = '20px';
        container.style.right = '20px';
        container.style.zIndex = '9999';
        container.style.display = 'flex';
        container.style.flexDirection = 'column';
        container.style.gap = '10px';
        document.body.appendChild(container);
    }
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.style.background = 'white';
    toast.style.padding = '1rem 1.5rem';
    toast.style.borderRadius = '8px';
    toast.style.boxShadow = 'var(--shadow-lg)';
    toast.style.borderLeft = `4px solid ${type === 'success' ? 'var(--success)' : 'var(--danger)'}`;
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '12px';
    toast.style.animation = 'slideInRight 0.3s ease';
    
    const icon = type === 'success' ? '<i class="fa-solid fa-circle-check" style="color:var(--success); font-size:1.2rem;"></i>' : '<i class="fa-solid fa-circle-xmark" style="color:var(--danger); font-size:1.2rem;"></i>';
    toast.innerHTML = `${icon} <span style="font-weight:500;">${message}</span>`;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/**
 * Unified fetch wrapper for handling JWTs
 * @param {string} url - The API endpoint
 * @param {object} options - Fetch options (method, body, headers)
 * @param {string} tokenKey - The local storage key for the token ('studentToken' or 'adminToken')
 * @param {string} loginRedirectUrl - The URL to redirect to if auth fails
 */
async function apiFetchWithAuth(url, options = {}, tokenKey, loginRedirectUrl) {
    const token = localStorage.getItem(tokenKey);
    if (!token && !url.includes('/login')) {
        window.location.href = loginRedirectUrl;
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };
    
    // Do not set content-type for FormData (browser sets multipart/form-data boundary automatically)
    if (options.body instanceof FormData) {
        delete headers['Content-Type']; 
    }

    try {
        const response = await fetch(url, { ...options, headers });
        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem(tokenKey);
            window.location.href = loginRedirectUrl;
            return;
        }
        return response;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}
