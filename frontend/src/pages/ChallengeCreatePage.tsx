import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import { useCreateChallenge } from '../hooks/useChallenges';

const challengeSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요.').max(200),
  description: z.string().optional(),
  durationDays: z.coerce.number().min(3, '최소 3일').max(90, '최대 90일'),
  startDate: z.string().min(1, '시작일을 선택해주세요.'),
  maxParticipants: z.coerce.number().min(2, '최소 2명').max(100, '최대 100명').optional(),
});

type ChallengeForm = z.infer<typeof challengeSchema>;

export default function ChallengeCreatePage() {
  const createChallenge = useCreateChallenge();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ChallengeForm>({
    resolver: zodResolver(challengeSchema),
    defaultValues: { durationDays: 7, maxParticipants: 50 },
  });

  const today = new Date().toISOString().split('T')[0];

  const onSubmit = (data: ChallengeForm) => {
    createChallenge.mutate({
      title: data.title,
      description: data.description,
      durationDays: data.durationDays,
      startDate: data.startDate,
      maxParticipants: data.maxParticipants,
    });
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-800">챌린지 만들기</h1>
        <Link to="/challenges" className="text-sm text-slate-500 hover:text-slate-700">
          목록으로
        </Link>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="bg-white rounded-2xl shadow-md p-6 space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">제목</label>
            <input
              type="text"
              {...register('title')}
              className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              placeholder="예: 7일 미래일기 챌린지"
            />
            {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">설명 (선택)</label>
            <textarea
              {...register('description')}
              rows={3}
              className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
              placeholder="챌린지에 대한 설명을 적어주세요."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">기간 (일)</label>
              <input
                type="number"
                {...register('durationDays')}
                className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                min={3}
                max={90}
              />
              {errors.durationDays && <p className="text-red-500 text-sm mt-1">{errors.durationDays.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">시작일</label>
              <input
                type="date"
                min={today}
                {...register('startDate')}
                className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
              {errors.startDate && <p className="text-red-500 text-sm mt-1">{errors.startDate.message}</p>}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">최대 참가 인원</label>
            <input
              type="number"
              {...register('maxParticipants')}
              className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              min={2}
              max={100}
            />
            {errors.maxParticipants && <p className="text-red-500 text-sm mt-1">{errors.maxParticipants.message}</p>}
          </div>

          <div className="bg-slate-50 rounded-lg p-4 text-sm text-slate-600">
            <p>&#9432; 실패 기준: 전체 기간의 15% 이상 미작성 시 실패 처리됩니다.</p>
          </div>
        </div>

        {createChallenge.isError && (
          <p className="text-red-500 text-sm text-center">챌린지 생성에 실패했습니다.</p>
        )}

        <button
          type="submit"
          disabled={createChallenge.isPending}
          className="w-full py-4 bg-indigo-600 text-white rounded-2xl font-medium text-lg hover:bg-indigo-700 transition-colors disabled:opacity-50"
        >
          {createChallenge.isPending ? '생성 중...' : '챌린지 생성'}
        </button>
      </form>
    </div>
  );
}
