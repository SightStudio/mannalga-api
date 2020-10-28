package com.taskforce.superinvention.document.meeting

import com.fasterxml.jackson.annotation.JsonFormat
import com.taskforce.superinvention.app.domain.club.Club
import com.taskforce.superinvention.app.domain.club.user.ClubUser
import com.taskforce.superinvention.app.domain.meeting.Meeting
import com.taskforce.superinvention.app.domain.region.Region
import com.taskforce.superinvention.app.domain.role.Role
import com.taskforce.superinvention.app.domain.user.User
import com.taskforce.superinvention.app.web.dto.club.ClubDto
import com.taskforce.superinvention.app.web.dto.club.ClubUserDto
import com.taskforce.superinvention.app.web.dto.meeting.MeetingAddRequestDto
import com.taskforce.superinvention.app.web.dto.meeting.MeetingDto
import com.taskforce.superinvention.app.web.dto.region.of
import com.taskforce.superinvention.app.web.dto.role.RoleDto
import com.taskforce.superinvention.common.util.extendFun.DATE_TIME_FORMAT
import com.taskforce.superinvention.common.util.extendFun.toBaseDateTime
import com.taskforce.superinvention.config.MockitoHelper
import com.taskforce.superinvention.config.documentation.ApiDocumentUtil
import com.taskforce.superinvention.config.documentation.ApiDocumentUtil.getDocumentRequest
import com.taskforce.superinvention.config.documentation.ApiDocumentUtil.getDocumentResponse
import com.taskforce.superinvention.config.test.ApiDocumentationTest
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.BDDMockito.anyLong
import org.mockito.BDDMockito.given
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

class MeetingDocumentation: ApiDocumentationTest() {

    @Test
    @WithMockUser(authorities = [Role.MEMBER])
    fun `만남 조회 기능`() {
        // given
        val clubSeq = 76L
        val pageable = PageRequest.of(0, 10)

        val club = ClubDto(
                seq = clubSeq,
                name = "club name",
                description = "club description",
                maximumNumber = 100,
                userCount = null,
                mainImageUrl = "asdasd.jpg"
        )

        val clubUser = ClubUserDto(
                seq = 512,
                userSeq = 1,
                club = club,
                roles = setOf(RoleDto(Role.RoleName.CLUB_MEMBER, "CLUB_ROLE"))
        )

        val meetingList = listOf(
                MeetingDto(
                        seq = 1,
                        title = "title",
                        content = "contents",
                        startTimestamp = LocalDateTime.now().toBaseDateTime(),
                        endTimestamp = LocalDateTime.now().plusDays(1).toBaseDateTime(),
                        club = club,
                        deleteFlag = false,
                        maximumNumber = 30,
                        regClubUser = clubUser
                )
        )

        val meetings: Page<MeetingDto> = PageImpl(meetingList, pageable, meetingList.size.toLong())


        given(roleService.hasClubMemberAuth(ArgumentMatchers.anyLong(), MockitoHelper.anyObject()))
                .willReturn(true)
        given(meetingService.getMeeting(clubSeq = clubSeq, pageable = pageable)).willReturn(meetings)

        // when
        val result: ResultActions = this.mockMvc.perform(
                get("/clubs/{clubSeq}/meetings", clubSeq)
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdXRoIjoiW1VTRVJdIi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(document("meeting-all", getDocumentRequest(), getDocumentResponse(),
                        pathParameters(parameterWithName("clubSeq").description("모임 시퀀스")),
                        responseFields(
                                *ApiDocumentUtil.commonResponseField(),
                                *ApiDocumentUtil.pageFieldDescriptor(),

                                fieldWithPath("data.content.[].seq").type(JsonFieldType.NUMBER).description("만남 시퀀스"),
                                fieldWithPath("data.content.[].title").type(JsonFieldType.STRING).description("만남 제목"),
                                fieldWithPath("data.content.[].content").type(JsonFieldType.STRING).description("만남 상세 내용"),
                                fieldWithPath("data.content.[].startTimestamp").type(JsonFieldType.STRING).description("만남 시작 시간. $DATE_TIME_FORMAT 형식으로 전송하면 받을 수 있다."),
                                fieldWithPath("data.content.[].endTimestamp").type(JsonFieldType.STRING).description("만남 종료 시간. $DATE_TIME_FORMAT 형식으로 전송하면 받을 수 있다."),
                                fieldWithPath("data.content.[].club").type(JsonFieldType.OBJECT).description("만남을 진행하는 모임 정보"),
                                fieldWithPath("data.content.[].club.seq").type(JsonFieldType.NUMBER).description("모임 시퀀스"),
                                fieldWithPath("data.content.[].club.name").type(JsonFieldType.STRING).description("모임 모임명"),
                                fieldWithPath("data.content.[].club.description").type(JsonFieldType.STRING).description("모임 상세설명"),
                                fieldWithPath("data.content.[].club.maximumNumber").type(JsonFieldType.NUMBER).description("모임 모임 최대 인원"),
                                fieldWithPath("data.content.[].club.mainImageUrl").type(JsonFieldType.STRING).description("모임 메인 이미지"),
                                fieldWithPath("data.content.[].club.userCount").type(JsonFieldType.NULL).description("사용하지 않는 필드"),
                                fieldWithPath("data.content.[].deleteFlag").type(JsonFieldType.BOOLEAN).description("만남 삭제 여부"),
                                fieldWithPath("data.content.[].maximumNumber").type(JsonFieldType.NUMBER).description("만남 최대 제한 인원"),
                                fieldWithPath("data.content.[].regClubUser").type(JsonFieldType.OBJECT).description("만남 생성한 모임원 정보"),
                                fieldWithPath("data.content.[].regClubUser.seq").type(JsonFieldType.NUMBER).description("모임원 시퀀스"),
                                fieldWithPath("data.content.[].regClubUser.userSeq").type(JsonFieldType.NUMBER).description("모임원의 유저 시퀀스"),
                                fieldWithPath("data.content.[].regClubUser.club").type(JsonFieldType.OBJECT).description("모임원의 모임 정보"),
                                fieldWithPath("data.content.[].regClubUser.club.seq").type(JsonFieldType.NUMBER).description("모임 시퀀스"),
                                fieldWithPath("data.content.[].regClubUser.club.name").type(JsonFieldType.STRING).description("모임 모임명"),
                                fieldWithPath("data.content.[].regClubUser.club.description").type(JsonFieldType.STRING).description("모임 상세설명"),
                                fieldWithPath("data.content.[].regClubUser.club.maximumNumber").type(JsonFieldType.NUMBER).description("모임 모임 최대 인원"),
                                fieldWithPath("data.content.[].regClubUser.club.mainImageUrl").type(JsonFieldType.STRING).description("모임 메인 이미지"),
                                fieldWithPath("data.content.[].regClubUser.club.userCount").type(JsonFieldType.NULL).description("사용하지 않는 필드"),
                                fieldWithPath("data.content.[].regClubUser.roles").type(JsonFieldType.ARRAY).description("모임원의 권한 정보"),
                                fieldWithPath("data.content.[].regClubUser.roles[].name").type(JsonFieldType.STRING).description("권한 명"),
                                fieldWithPath("data.content.[].regClubUser.roles.[].roleGroupName").type(JsonFieldType.STRING).description("권한그룹 명")
                        )
                ))
    }

    @Test
    @WithMockUser(authorities = [Role.MEMBER])
    fun `만남 생성`() {
        // given
        val clubSeq = 76L
        val meetingAddRequestDto = MeetingAddRequestDto(
                title = "test title",
                content = "test content",
                startTimestamp = LocalDateTime.parse("2020-08-08T10:00:00"),
                endTimestamp = LocalDateTime.parse("2020-08-09T11:00:00"),
                maximumNumber = 20
        )


        val club = Club("name", "desc", 3L, "sdasd.jpg")
        val clubUser = ClubUser(
                club,
                User("eric")
        )

        val clubDto = ClubDto(
                seq = clubSeq,
                name = "club name",
                description = "club description",
                maximumNumber = 100,
                userCount = null,
                mainImageUrl = "asdasd.jpg"
        )
        clubUser.seq = 132L

        val clubUserDto = ClubUserDto(
                seq = 512,
                userSeq = 1,
                club = clubDto,
                roles = setOf(RoleDto(Role.RoleName.CLUB_MEMBER, "CLUB_ROLE"))
        )
        val meetingDto = MeetingDto(
                seq = 1,
                title = "title",
                content = "contents",
                startTimestamp = LocalDateTime.now().toBaseDateTime(),
                endTimestamp = LocalDateTime.now().plusDays(1).toBaseDateTime(),
                club = clubDto,
                deleteFlag = false,
                maximumNumber = 30,
                regClubUser = clubUserDto
        )
        given(clubService.getClubUser(ArgumentMatchers.anyLong(), MockitoHelper.anyObject())).willReturn(clubUser)
        given(roleService.hasClubManagerAuth(MockitoHelper.anyObject())).willReturn(true)
        given(meetingService.createMeeting(MockitoHelper.anyObject(), ArgumentMatchers.anyLong())).willReturn(meetingDto)

        // when
        val result: ResultActions = this.mockMvc.perform(
                post("/clubs/{clubSeq}/meetings", clubSeq)
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdXRoIjoiW1VTRVJdIi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meetingAddRequestDto))
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(document("create-meeting", getDocumentRequest(), getDocumentResponse(),
                        pathParameters(parameterWithName("clubSeq").description("모임 시퀀스")),
                        responseFields(
                                *ApiDocumentUtil.commonResponseField(),

                                fieldWithPath("data.seq").type(JsonFieldType.NUMBER).description("만남 시퀀스"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("만남 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("만남 상세 내용"),
                                fieldWithPath("data.startTimestamp").type(JsonFieldType.STRING).description("만남 시작 시간. $DATE_TIME_FORMAT 형식으로 전송하면 받을 수 있다."),
                                fieldWithPath("data.endTimestamp").type(JsonFieldType.STRING).description("만남 종료 시간. $DATE_TIME_FORMAT 형식으로 전송하면 받을 수 있다."),
                                fieldWithPath("data.club").type(JsonFieldType.OBJECT).description("만남을 진행하는 모임 정보"),
                                fieldWithPath("data.club.seq").type(JsonFieldType.NUMBER).description("모임 시퀀스"),
                                fieldWithPath("data.club.name").type(JsonFieldType.STRING).description("모임 모임명"),
                                fieldWithPath("data.club.description").type(JsonFieldType.STRING).description("모임 상세설명"),
                                fieldWithPath("data.club.maximumNumber").type(JsonFieldType.NUMBER).description("모임 모임 최대 인원"),
                                fieldWithPath("data.club.mainImageUrl").type(JsonFieldType.STRING).description("모임 메인 이미지"),
                                fieldWithPath("data.club.userCount").type(JsonFieldType.NULL).description("사용하지 않는 필드"),
                                fieldWithPath("data.deleteFlag").type(JsonFieldType.BOOLEAN).description("만남 삭제 여부"),
                                fieldWithPath("data.maximumNumber").type(JsonFieldType.NUMBER).description("만남 최대 제한 인원"),
                                fieldWithPath("data.regClubUser").type(JsonFieldType.OBJECT).description("만남 생성한 모임원 정보"),
                                fieldWithPath("data.regClubUser.seq").type(JsonFieldType.NUMBER).description("모임원 시퀀스"),
                                fieldWithPath("data.regClubUser.userSeq").type(JsonFieldType.NUMBER).description("모임원의 유저 시퀀스"),
                                fieldWithPath("data.regClubUser.club").type(JsonFieldType.OBJECT).description("모임원의 모임 정보"),
                                fieldWithPath("data.regClubUser.club.seq").type(JsonFieldType.NUMBER).description("모임 시퀀스"),
                                fieldWithPath("data.regClubUser.club.name").type(JsonFieldType.STRING).description("모임 모임명"),
                                fieldWithPath("data.regClubUser.club.description").type(JsonFieldType.STRING).description("모임 상세설명"),
                                fieldWithPath("data.regClubUser.club.maximumNumber").type(JsonFieldType.NUMBER).description("모임 모임 최대 인원"),
                                fieldWithPath("data.regClubUser.club.mainImageUrl").type(JsonFieldType.STRING).description("모임 메인 이미지"),
                                fieldWithPath("data.regClubUser.club.userCount").type(JsonFieldType.NULL).description("사용하지 않는 필드"),
                                fieldWithPath("data.regClubUser.roles").type(JsonFieldType.ARRAY).description("모임원의 권한 정보"),
                                fieldWithPath("data.regClubUser.roles[].name").type(JsonFieldType.STRING).description("권한 명"),
                                fieldWithPath("data.regClubUser.roles.[].roleGroupName").type(JsonFieldType.STRING).description("권한그룹 명")
                        )
                ))


    }

}