import { useState } from 'react'
import { generateBeneficiaryNotifications } from '../data/beneficiary-notifications'
import { Bell, Check, Trash2 } from 'lucide-react'
import '../styles/pages/beneficiary-notifications.css'

export function BeneficiaryNotifications() {
  const [notifications, setNotifications] = useState(generateBeneficiaryNotifications())
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
    <div className="beneficiary-notifications">
      <div className="notifications-header">
        <div className="header-title">
          <Bell size={24} />
          <div>
            <h2>Centro de Notificaciones</h2>
            <p>{unreadCount} notificaciones sin leer</p>
          </div>
        </div>
        {unreadCount > 0 && (
          <button className="btn-mark-all" onClick={handleMarkAllAsRead}>
            Marcar todas como leídas
          </button>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="notification-filters">
        <button 
          className={`filter-tab ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          Todas ({notifications.length})
        </button>
        <button 
          className={`filter-tab ${filter === 'unread' ? 'active' : ''}`}
          onClick={() => setFilter('unread')}
        >
          Sin leer ({unreadCount})
        </button>
        <button 
          className={`filter-tab ${filter === 'read' ? 'active' : ''}`}
          onClick={() => setFilter('read')}
        >
          Leídas ({notifications.filter(n => n.read).length})
        </button>
      </div>

      {/* Notifications List */}
      <div className="notifications-list">
        {filteredNotifications.length > 0 ? (
          filteredNotifications.map(notif => (
            <div 
              key={notif.id} 
              className={`notification-item ${!notif.read ? 'unread' : ''}`}
              style={{ borderLeftColor: getNotificationColor(notif.type) }}
            >
              <div className="notification-icon">
                {notif.icon}
              </div>
              <div className="notification-content">
                <h4>{notif.title}</h4>
                <p>{notif.message}</p>
                <small className="notification-time">
                  {formatTime(notif.timestamp)}
                </small>
              </div>
              <div className="notification-actions">
                {!notif.read && (
                  <button 
                    className="action-btn read-btn"
                    onClick={() => handleMarkAsRead(notif.id)}
                    title="Marcar como leída"
                  >
                    <Check size={18} />
                  </button>
                )}
                <button 
                  className="action-btn delete-btn"
                  onClick={() => handleDelete(notif.id)}
                  title="Eliminar"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <Bell size={48} />
            <h3>No hay notificaciones</h3>
            <p>No tienes notificaciones en este momento</p>
          </div>
        )}
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
