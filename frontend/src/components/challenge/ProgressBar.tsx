interface Props {
  percent: number;
  status: 'ACTIVE' | 'COMPLETED' | 'FAILED';
}

export default function ProgressBar({ percent, status }: Props) {
  const colorMap = {
    ACTIVE: 'bg-indigo-500',
    COMPLETED: 'bg-green-500',
    FAILED: 'bg-red-500',
  };

  return (
    <div className="w-full bg-slate-200 rounded-full h-3">
      <div
        className={`h-3 rounded-full transition-all duration-500 ${colorMap[status]}`}
        style={{ width: `${Math.min(percent, 100)}%` }}
      />
    </div>
  );
}
