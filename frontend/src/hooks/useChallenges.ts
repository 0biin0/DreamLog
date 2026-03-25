import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { challengeApi } from '../api/challengeApi';
import type { ChallengeCreateRequest } from '../types/challenge';

export function useJoinableChallenges() {
  return useQuery({
    queryKey: ['challenges', 'joinable'],
    queryFn: () => challengeApi.getJoinable().then((res) => res.data.data!),
  });
}

export function useChallenge(id: number) {
  return useQuery({
    queryKey: ['challenge', id],
    queryFn: () => challengeApi.getOne(id).then((res) => res.data.data!),
  });
}

export function useMyChallenges() {
  return useQuery({
    queryKey: ['challenges', 'my'],
    queryFn: () => challengeApi.getMy().then((res) => res.data.data!),
  });
}

export function useChallengeProgress(id: number) {
  return useQuery({
    queryKey: ['challenge', id, 'progress'],
    queryFn: () => challengeApi.getProgress(id).then((res) => res.data.data!),
  });
}

export function useCreateChallenge() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: ChallengeCreateRequest) => challengeApi.create(data),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['challenges'] });
      navigate(`/challenges/${res.data.data!.id}`);
    },
  });
}

export function useJoinChallenge() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => challengeApi.join(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['challenges'] });
    },
  });
}
