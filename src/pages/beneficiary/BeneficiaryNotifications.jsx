import { useState } from 'react'
import { generateBeneficiaryNotifications } from '../../data/beneficiary-notifications'
import { Bell, Check, Trash2 } from 'lucide-react'
import '../../styles/pages/beneficiary-notifications.css'

export function BeneficiaryNotifications({ notifications: initialNotifications }) {
  const [notifications, setNotifications] = useState(initialNotifications || generateBeneficiaryNotifications())
  const [filter, setFilter] = useState('all')

  const filteredNotifications = filter === 'all' 
    ? notifications
    : filter === 'unread'
    ? notifications.filter(n => !n.read)
    : notifications.filter(n => n.read)

  const unreadCount = notifications.filter(n => !n.read).length

  const handleMarkAsRead = (id) => {
    setNotifications(notifications.map(n => 
      n.id === id ? { ...n, read: true } : n
    ))
  }

  const handleDelete = (id) => {
    setNotifications(notifications.filter(n => n.id !== id))
  }

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })))
  }

  const getNotificationIcon = (type) => {
    const icons = {
      donation_assigned: '📦',
      delivery_in_transit: '🚚',
      delivery_arrived: '📍',
      delivery_confirmed: '✅',
      need_matched: '💚'
    }
    return icons[type] || '🔔'
  }

  const getNotificationColor = (type) => {
    const colors = {
      donation_assigned: '#3498db',
      delivery_in_transit: '#f39c12',
      delivery_arrived: '#e74c3c',
      delivery_confirmed: '#2ecc71',
      need_matched: '#9b59b6'
    }
    return colors[type] || '#95a5a6'
  }

  return (
    <div className="notifications-view">
      <div className="section-header">
        <h2>Centro de Notificaciones</h2>
        <p>{unreadCount} notificaciones sin leer</p>
      </div>

      <div className="notifications-container">
        {/* Sidebar Filters */}
        <div className="notifications-sidebar">
          <div className="notification-filter">
            <div className="filter-header">
              <h3>Filtrar</h3>
            </div>
            <div className="filter-options">
              <button
                className={`filter-option ${filter === 'all' ? 'active' : ''}`}
                onClick={() => setFilter('all')}
              >
                Todas
                <span className="filter-option-count">{notifications.length}</span>
              </button>
              <button
                className={`filter-option ${filter === 'unread' ? 'active' : ''}`}
                onClick={() => setFilter('unread')}
              >
                Sin leer
                <span className="filter-option-count">{unreadCount}</span>
              </button>
              <button
                className={`filter-option ${filter === 'read' ? 'active' : ''}`}
                onClick={() => setFilter('read')}
              >
                Leídas
                <span className="filter-option-count">{notifications.filter(n => n.read).length}</span>
              </button>
            </div>
          </div>

          <div className="action-buttons">
            {unreadCount > 0 && (
              <button className="btn-mark-all-read" onClick={handleMarkAllAsRead}>
                Marcar todo como leído
              </button>
            )}
            <button className="btn-clear-all" onClick={() => setNotifications([])}>
              Limpiar todas
            </button>
          </div>
        </div>

        {/* Notifications List */}
        <div className="notifications-content">
          <div className="content-header">
            <h3>Notificaciones ({filteredNotifications.length})</h3>
          </div>

          <div className="notifications-list">
            {filteredNotifications.length > 0 ? (
              filteredNotifications.map(notif => (
                <div
                  key={notif.id}
                  className={`notification-item ${!notif.read ? 'unread' : ''}`}
                >
                  <div className="notification-icon" style={{ background: getNotificationColor(notif.type) + '20' }}>
                    {getNotificationIcon(notif.type)}
                  </div>
                  <div className="notification-content">
                    <div className="notification-header">
                      <strong>{notif.title}</strong>
                      <span className="notification-time">{formatTime(notif.timestamp)}</span>
                    </div>
                    <div className="notification-body">{notif.message}</div>
                  </div>
                  <div className="notification-actions">
                    {!notif.read && (
                      <button
                        className="btn-action"
                        onClick={() => handleMarkAsRead(notif.id)}
                        title="Marcar como leída"
                      >
                        <Check size={16} />
                      </button>
                    )}
                    <button
                      className="btn-action btn-delete"
                      onClick={() => handleDelete(notif.id)}
                      title="Eliminar"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <div className="empty-icon">🔔</div>
                <h3>No hay notificaciones</h3>
                <p>No tienes notificaciones en este momento</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function formatTime(timestamp) {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return 'Hace unos segundos'
  if (minutes < 60) return `Hace ${minutes} min`
  if (hours < 24) return `Hace ${hours} h`
  if (days < 7) return `Hace ${days} días`
  
  return date.toLocaleDateString('es-AR')
}
