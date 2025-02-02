import { rest } from "msw";
import { getHTTPBaseAPIUrl } from "src/config";
import { MswInstance } from "src/services/api-mocks/types";
import { TeamNamesGetResponse } from "src/domain/team/team-types";

function mockGetTeams({
  mswInstance,
  response,
}: {
  mswInstance: MswInstance;
  response: {
    status?: number;
    data: TeamNamesGetResponse | { message: string };
  };
}) {
  const url = `${getHTTPBaseAPIUrl()}/getAllTeamsSUOnly`;
  mswInstance.use(
    rest.get(url, async (req, res, ctx) => {
      return res(ctx.status(response.status ?? 200), ctx.json(response.data));
    })
  );
}

const mockedTeamResponse = ["TEST_TEAM_01", "TEST_TEAM_02", "TEST_TEAM_03"];
export { mockGetTeams, mockedTeamResponse };
