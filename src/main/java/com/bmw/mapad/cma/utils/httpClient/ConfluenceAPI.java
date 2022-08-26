package com.bmw.mapad.cma.utils.httpClient;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Retrofit interface to establish connection with Confluence rest api.
 * For further information about the concepts below-mentioned consider visit Confluence Rest API official documentation.
 */
public interface ConfluenceAPI {

    /**
     * Returns a piece of content. Content can be a page, sub-page,etc.
     */
    @GET("content/{contentId}")
    Call<ResponseBody> getContent(@Path("contentId") String contentId);

    /**
     * Returns all the attachments placed within the specified content.
     */
    @GET("content/{contentId}/child/attachment")
    Call<ResponseBody> getAttachments(@Path("contentId") String contentId);

    /**
     * Add one attachment to a Confluence Content entity.
     */
    @POST("content/{contentId}/child/attachment")
    Call<ResponseBody> createAttachment(@Path("contentId") String contentId);

    /**
     * Update an attachment with a new version from a Confluence Content entity.
     */
    @Multipart
    @POST("content/{contentId}/child/attachment/{attachmentId}/data")
    Call<ResponseBody> updateAttachment(@Path("contentId") String contentId,
                                        @Path("attachmentId") String attachmentId,
                                        @Part MultipartBody.Part file);


}
