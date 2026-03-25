import { Link } from 'react-router-dom';

interface Props {
  icon: string;
  title: string;
  description?: string;
  actionLabel?: string;
  actionTo?: string;
}

export default function EmptyState({ icon, title, description, actionLabel, actionTo }: Props) {
  return (
    <div className="text-center py-16">
      <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <span className="text-3xl">{icon}</span>
      </div>
      <p className="text-slate-600 font-medium mb-2">{title}</p>
      {description && <p className="text-sm text-slate-400 mb-4">{description}</p>}
      {actionLabel && actionTo && (
        <Link
          to={actionTo}
          className="text-indigo-600 font-medium hover:underline text-sm"
        >
          {actionLabel}
        </Link>
      )}
    </div>
  );
}
