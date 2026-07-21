interface BadgeProps {
  children: React.ReactNode;
  tone?: 'neutral' | 'low' | 'medium' | 'high' | 'critical';
}

const TONE_STYLES: Record<string, string> = {
  neutral: 'bg-gray-100 text-gray-600',
  low: 'bg-slate-100 text-slate-600',
  medium: 'bg-amber-100 text-amber-700',
  high: 'bg-orange-100 text-orange-700',
  critical: 'bg-red-100 text-red-700',
};

export default function Badge({ children, tone = 'neutral' }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded text-xs uppercase tracking-wide ${TONE_STYLES[tone]}`}
      style={{ fontFamily: "'JetBrains Mono', monospace" }}
    >
      {children}
    </span>
  );
}