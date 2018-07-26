package br.com.lfdb.particity.api;

import br.com.lfdb.particity.api.model.ReportItemRequest;
import br.com.lfdb.particity.api.model.ReportItemResponse;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ZupService {

    @POST("/reports/{categoryId}/items")
    ReportItemResponse createReport(@Path("categoryId") long categoryId, @Body ReportItemRequest item);
}
