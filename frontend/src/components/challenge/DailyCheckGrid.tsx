import type { DailyLogEntry } from '../../types/challenge';

interface Props {
  logs: DailyLogEntry[];
}

export default function DailyCheckGrid({ logs }: Props) {
  return (
    <div className="grid grid-cols-7 gap-2">
      {logs.map((log) => (
        <div
          key={log.date}
          className={`w-10 h-10 rounded-lg flex items-center justify-center text-xs font-medium ${
            log.achieved
              ? 'bg-green-100 text-green-700'
              : 'bg-red-100 text-red-500'
          }`}
          title={`${log.date}: ${log.achieved ? '성공' : '미달성'}`}
        >
          {log.achieved ? '&#10003;' : '&#10007;'}
        </div>
      ))}
    </div>
  );
}
