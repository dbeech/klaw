import api from "src/services/api";
import {
  TopicApiResponse,
  TopicDTOApiResponse,
} from "src/domain/topic/topic-types";
import { transformTopicApiResponse } from "src/domain/topic/topic-transformer";
import { Environment } from "src/domain/environment";

const getTopics = async ({
  currentPage = 1,
  environment = "ALL",
  teamName,
}: {
  currentPage: number;
  environment: Environment;
  teamName?: string;
}): Promise<TopicApiResponse> => {
  const team = teamName && teamName !== "All teams" ? teamName : null;
  const params: Record<string, string> = {
    pageNo: currentPage.toString(),
    env: environment,
    ...(team && { teamName: team }),
  };

  return api
    .get<TopicDTOApiResponse>(`/getTopics?${new URLSearchParams(params)}`)
    .then(transformTopicApiResponse);
};

export { getTopics };