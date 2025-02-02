import { QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { ReactElement } from "react";
import { server } from "src/services/api-mocks/server";
import { useGetTeams } from "src/app/features/browse-topics/hooks/teams/useGetTeams";
import { mockedTeamResponse, mockGetTeams } from "src/domain/team/team-api.msw";
import { getQueryClientForTests } from "src/services/test-utils/query-client-tests";

const wrapper = ({ children }: { children: ReactElement }) => (
  <QueryClientProvider client={getQueryClientForTests()}>
    {children}
  </QueryClientProvider>
);

describe("useGetTeams", () => {
  const originalConsoleError = console.error;

  beforeAll(() => {
    server.listen();
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    console.error = originalConsoleError;
    server.close();
  });

  describe("handles loading and error state", () => {
    it("returns a loading state before starting to fetch data", async () => {
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });
      expect(result.current.isLoading).toBe(true);

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });
    });

    it("returns an error when request fails", async () => {
      console.error = jest.fn();

      mockGetTeams({
        mswInstance: server,
        response: { status: 400, data: { message: "" } },
      });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
      expect(console.error).toHaveBeenCalledTimes(1);
    });
  });

  describe("handles successful response", () => {
    it("returns a list of teams", async () => {
      mockGetTeams({
        mswInstance: server,
        response: { data: mockedTeamResponse },
      });

      const { result } = await renderHook(() => useGetTeams(), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual([
        "TEST_TEAM_01",
        "TEST_TEAM_02",
        "TEST_TEAM_03",
      ]);
    });
  });
});
