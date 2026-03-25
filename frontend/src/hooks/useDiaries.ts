import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { diaryApi } from '../api/diaryApi';
import type { DiaryCreateRequest, DiaryUpdateRequest } from '../types/diary';

export function useDiaryList(page = 0) {
  return useQuery({
    queryKey: ['diaries', page],
    queryFn: () => diaryApi.getList(page).then((res) => res.data.data!),
  });
}

export function useDiary(id: number) {
  return useQuery({
    queryKey: ['diary', id],
    queryFn: () => diaryApi.getOne(id).then((res) => res.data.data!),
  });
}

export function useArrivedDiaries() {
  return useQuery({
    queryKey: ['diaries', 'arrived'],
    queryFn: () => diaryApi.getArrived().then((res) => res.data.data!),
  });
}

export function useCreateDiary() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: DiaryCreateRequest) => diaryApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['diaries'] });
      navigate('/diary/list');
    },
  });
}

export function useUpdateDiary(id: number) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: DiaryUpdateRequest) => diaryApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['diary', id] });
      queryClient.invalidateQueries({ queryKey: ['diaries'] });
      navigate(`/diary/${id}`);
    },
  });
}

export function useDeleteDiary() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (id: number) => diaryApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['diaries'] });
      navigate('/diary/list');
    },
  });
}
