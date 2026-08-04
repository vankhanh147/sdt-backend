-- Removes only Task 008 feedback seed data identified by raw_feedback.source_ref.
-- Safe order: analysis_result -> feedback -> raw_feedback.

BEGIN;

DELETE FROM public.analysis_result AS analysis
USING public.feedback AS feedback,
      public.raw_feedback AS raw
WHERE analysis.feedback_id = feedback.id
  AND feedback.raw_feedback_id = raw.id
  AND raw.source_ref LIKE 'SEED-LLM-%';

DELETE FROM public.feedback AS feedback
USING public.raw_feedback AS raw
WHERE feedback.raw_feedback_id = raw.id
  AND raw.source_ref LIKE 'SEED-LLM-%';

DELETE FROM public.raw_feedback
WHERE source_ref LIKE 'SEED-LLM-%';

COMMIT;
