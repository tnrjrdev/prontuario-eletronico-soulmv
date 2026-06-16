import React, { type ButtonHTMLAttributes, type InputHTMLAttributes, type SelectHTMLAttributes } from 'react'
import { cn } from '../utils/cn'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline' | 'ghost'
  size?: 'default' | 'sm' | 'lg' | 'icon'
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'default', ...props }, ref) => {
    const variants = {
      primary: 'bg-primary text-primary-foreground hover:bg-primary/90',
      secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
      danger: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
      outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
      ghost: 'hover:bg-accent hover:text-accent-foreground',
    }
    const sizes = {
      default: 'h-10 px-4 py-2',
      sm: 'h-9 rounded-md px-3',
      lg: 'h-11 rounded-md px-8',
      icon: 'h-10 w-10',
    }
    return (
      <button
        ref={ref}
        className={cn(
          'inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
          variants[variant],
          sizes[size],
          className
        )}
        {...props}
      />
    )
  }
)
Button.displayName = 'Button'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, label, ...props }, ref) => {
    return (
      <div className="space-y-1.5 w-full">
        {label && <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">{label}</label>}
        <input
          type={type}
          className={cn(
            'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
            className
          )}
          ref={ref}
          {...props}
        />
      </div>
    )
  }
)
Input.displayName = 'Input'

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, label, children, ...props }, ref) => {
    return (
      <div className="space-y-1.5 w-full">
        {label && <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">{label}</label>}
        <select
          className={cn(
            'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
            className
          )}
          ref={ref}
          {...props}
        >
          {children}
        </select>
      </div>
    )
  }
)
Select.displayName = 'Select'

export function Card({ className, children, title, footer }: { className?: string, children: React.ReactNode; title?: React.ReactNode, footer?: React.ReactNode }) {
  return (
    <div className={cn("rounded-lg border bg-card text-card-foreground shadow-sm", className)}>
      {title && (
        <div className="flex flex-col space-y-1.5 p-6 pb-3">
          <h3 className="text-lg font-semibold leading-none tracking-tight">{title}</h3>
        </div>
      )}
      <div className="p-6 pt-3">{children}</div>
      {footer && <div className="flex items-center p-6 pt-0">{footer}</div>}
    </div>
  )
}

export type BadgeColor = 'slate' | 'green' | 'red' | 'yellow' | 'blue' | 'orange' | 'stable' | 'attention' | 'critical' | 'info'

const BADGE_CLASSES: Record<BadgeColor, string> = {
  slate: 'bg-slate-100 text-slate-800',
  green: 'bg-emerald-100 text-emerald-800',
  red: 'bg-rose-100 text-rose-800',
  yellow: 'bg-amber-100 text-amber-800',
  blue: 'bg-blue-100 text-blue-800',
  orange: 'bg-orange-100 text-orange-800',
  stable: 'bg-emerald-100 text-emerald-800',
  attention: 'bg-amber-100 text-amber-800',
  critical: 'bg-rose-100 text-rose-800',
  info: 'bg-blue-100 text-blue-800',
}

export function Badge({ children, color = 'slate', variant = 'solid', className }: { children: React.ReactNode; color?: BadgeColor; variant?: 'solid' | 'outline'; className?: string }) {
  const colorCls = variant === 'outline' ? 'border-current bg-transparent' : BADGE_CLASSES[color]
  return (
    <span className={cn('inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2', colorCls, className)}>
      {children}
    </span>
  )
}

export function Spinner({ className }: { className?: string }) {
  return <div className={cn("h-6 w-6 animate-spin rounded-full border-2 border-muted border-t-primary", className)} />
}

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn('animate-pulse rounded-md bg-muted', className)} />
}

export function EmptyState({
  icon,
  title,
  description,
  action,
  className,
}: {
  icon?: React.ReactNode
  title: string
  description?: React.ReactNode
  action?: React.ReactNode
  className?: string
}) {
  return (
    <div className={cn('flex flex-col items-center justify-center text-center py-12 text-muted-foreground', className)}>
      {icon && <div className="mb-3 opacity-20 [&>svg]:h-12 [&>svg]:w-12">{icon}</div>}
      <h3 className="text-lg font-semibold text-foreground">{title}</h3>
      {description && <p className="mt-1 max-w-md text-sm">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

export function Avatar({
  name,
  size = 'md',
  className,
}: {
  name?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}) {
  const initials = (name ?? '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0])
    .join('')
    .toUpperCase() || '?'
  const sizes = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-12 w-12 text-base',
  }
  return (
    <span
      className={cn(
        'inline-flex shrink-0 items-center justify-center rounded-full bg-primary/10 font-bold text-primary',
        sizes[size],
        className
      )}
      aria-hidden
    >
      {initials}
    </span>
  )
}

export function Alert({ children, variant = 'default', className }: { children: React.ReactNode, variant?: 'default' | 'destructive', className?: string }) {
  return (
    <div className={cn("relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg+div]:translate-y-[-3px] [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground", 
      variant === 'destructive' ? 'border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive' : 'bg-background text-foreground',
      className
    )}>
      <div className="text-sm [&_p]:leading-relaxed">
        {children}
      </div>
    </div>
  )
}
