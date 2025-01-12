package org.openedx.course.presentation.unit.container

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.openedx.core.FragmentViewType
import org.openedx.core.domain.model.Block
import org.openedx.course.presentation.unit.NotSupportedUnitFragment
import org.openedx.course.presentation.unit.html.HtmlUnitFragment
import org.openedx.course.presentation.unit.video.VideoUnitFragment
import org.openedx.course.presentation.unit.video.YoutubeVideoUnitFragment
import org.openedx.discussion.presentation.threads.DiscussionThreadsFragment
import org.openedx.discussion.presentation.topics.DiscussionTopicsFragment

class CourseUnitContainerAdapter(
    fragment: Fragment,
    val blocks: List<Block>,
    private val viewModel: CourseUnitContainerViewModel,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = blocks.size

    override fun createFragment(position: Int): Fragment = unitBlockFragment(blocks[position])

    private fun unitBlockFragment(block: Block): Fragment {
        return when {
            (block.isVideoBlock &&
                    (block.studentViewData?.encodedVideos?.hasVideoUrl == true ||
                            block.studentViewData?.encodedVideos?.hasYoutubeUrl == true)) -> {
                val encodedVideos = block.studentViewData?.encodedVideos!!
                val transcripts = block.studentViewData!!.transcripts
                with(encodedVideos) {
                    var isDownloaded = false
                    val videoUrl = if (viewModel.getDownloadModelById(block.id) != null) {
                        isDownloaded = true
                        viewModel.getDownloadModelById(block.id)!!.path
                    } else videoUrl
                    if (videoUrl.isNotEmpty()) {
                        VideoUnitFragment.newInstance(
                            block.id,
                            viewModel.courseId,
                            videoUrl,
                            transcripts?.toMap() ?: emptyMap(),
                            block.displayName,
                            isDownloaded
                        )
                    } else {
                        YoutubeVideoUnitFragment.newInstance(
                            block.id,
                            viewModel.courseId,
                            encodedVideos.youtube?.url ?: "",
                            transcripts?.toMap() ?: emptyMap(),
                            block.displayName
                        )
                    }
                }
            }

            (block.isDiscussionBlock && block.studentViewData?.topicId.isNullOrEmpty().not()) -> {
                DiscussionThreadsFragment.newInstance(
                    DiscussionTopicsFragment.TOPIC,
                    viewModel.courseId,
                    block.studentViewData?.topicId ?: "",
                    block.displayName,
                    FragmentViewType.MAIN_CONTENT.name,
                    block.id
                )
            }

            block.studentViewMultiDevice.not() -> {
                NotSupportedUnitFragment.newInstance(
                    block.id,
                    block.lmsWebUrl
                )
            }

            block.isHTMLBlock ||
                    block.isProblemBlock ||
                    block.isOpenAssessmentBlock ||
                    block.isDragAndDropBlock ||
                    block.isWordCloudBlock ||
                    block.isLTIConsumerBlock ||
                    block.isSurveyBlock -> {
                HtmlUnitFragment.newInstance(block.id, block.studentViewUrl)
            }

            else -> {
                NotSupportedUnitFragment.newInstance(
                    block.id,
                    block.lmsWebUrl
                )
            }
        }
    }
}
