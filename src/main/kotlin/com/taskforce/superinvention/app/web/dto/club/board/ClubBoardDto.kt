package com.taskforce.superinvention.app.web.dto.club.board

import com.taskforce.superinvention.app.domain.club.board.ClubBoard
import com.taskforce.superinvention.app.domain.club.board.img.ClubBoardImg
import com.taskforce.superinvention.app.web.dto.club.ClubWriter
import com.taskforce.superinvention.app.web.dto.club.board.img.ClubBoardImgEditS3Path
import com.taskforce.superinvention.common.util.aws.s3.S3Path
import com.taskforce.superinvention.common.util.extendFun.sliceIfExceed
import com.taskforce.superinvention.common.util.extendFun.toBaseDateTime
import javax.validation.constraints.NotBlank

data class ClubBoardRegisterBody(
    @get:NotBlank(message = "게시판 제목을 입력해주세요")
    val title   : String,

    @get:NotBlank(message = "게시판 내용은 공백일 수 없습니다")
    val content : String,

    val category: ClubBoard.Category,
    val imgList : List<S3Path> = emptyList()
)

data class ClubBoardEditBody(
    val title   : String?,
    val content : String?,
    val category: ClubBoard.Category?,

    val imageList : List<ClubBoardImgEditS3Path> = emptyList(),
)

data class ClubBoardSearchOpt(
    val title  : String = "",
    val content: String = "",
)

// 모임 게시판 단일 조회
data class ClubBoardDto(
    val boardSeq   : Long,
    val title      : String,
    val content    : String,
    val category   : ClubBoard.Category,
    val likeCnt    : Long,
    val commentCnt : Long,
    val createdAt  : String,
    val updatedAt  : String,
    val isLiked    : Boolean,
    val imageList  : List<ClubBoardImgDto>,
    val writer     : ClubWriter
) {
    constructor(clubBoard: ClubBoard, boardImgList: List<ClubBoardImgDto> ,isLiked: Boolean): this(
        boardSeq   = clubBoard.seq!!,
        title      = clubBoard.title,
        content    = clubBoard.content,
        category   = clubBoard.category,
        likeCnt    = clubBoard.boardLikeCnt    ?: 0,
        commentCnt = clubBoard.boardCommentCnt ?: 0,
        createdAt  = clubBoard.createdAt?.toBaseDateTime() ?: "",
        updatedAt  = clubBoard.updatedAt?.toBaseDateTime() ?: "",
        imageList  = boardImgList,
        writer     = ClubWriter(clubBoard.clubUser),
        isLiked    = isLiked
    )
}

// 모임 게시판 리스트 조회
data class ClubBoardListViewDto(
    val boardSeq     : Long,
    val title        : String,
    val simpleContent: String,
    var mainImageUrl : String ?= "",
    val createAt     : String,
    val category     : String,
    val likeCnt      : Long,
    val commentCnt   : Long,
    val writer       : ClubWriter

) {
    constructor(clubBoard: ClubBoard): this(
        boardSeq = clubBoard.seq!!,
        title    = clubBoard.title,
        simpleContent = clubBoard.content.sliceIfExceed(0 until 50),
        mainImageUrl  = clubBoard.boardImgs.firstOrNull()?.imgUrl ?: "",
        createAt      = clubBoard.createdAt?.toBaseDateTime()     ?: "",
        category   = clubBoard.category.label,
        likeCnt    = clubBoard.boardLikeCnt    ?: 0,
        commentCnt = clubBoard.boardCommentCnt ?: 0,
        writer     = ClubWriter(clubBoard.clubUser)
    )

    constructor(imgHost: String, clubBoard: ClubBoard): this(
        boardSeq      = clubBoard.seq!!,
        title         = clubBoard.title,
        simpleContent = clubBoard.content.sliceIfExceed(0 until 50),
        createAt      = clubBoard.createdAt?.toBaseDateTime()     ?: "",
        category      = clubBoard.category.label,
        likeCnt       = clubBoard.boardLikeCnt    ?: 0,
        commentCnt    = clubBoard.boardCommentCnt ?: 0,
        writer        = ClubWriter(clubBoard.clubUser)
    ) {
        val firstImg: ClubBoardImg? = clubBoard.boardImgs.filterNot{ boardImg -> boardImg.deleteFlag }.firstOrNull()
        mainImageUrl = if(firstImg != null) {
            "${imgHost}/${firstImg.imgUrl}"
        } else {
            ""
        }
    }
}

data class ClubBoardImgDto(
    val imgSeq   : Long,
    val img      : S3Path,
    val createdAt: String,
) {
    constructor(imgHost: String, clubBoardImg: ClubBoardImg): this(
        imgSeq = clubBoardImg.seq!!,
        img    = S3Path(
            absolutePath = "${imgHost}/${clubBoardImg.imgUrl}",
            filePath     = clubBoardImg.imgUrl,
            fileName     = clubBoardImg.imgName
        ),
        createdAt = clubBoardImg.createdAt?.toBaseDateTime() ?: "",
    )
}
